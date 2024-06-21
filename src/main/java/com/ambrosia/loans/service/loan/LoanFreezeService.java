package com.ambrosia.loans.service.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.DLoanMeta;
import com.ambrosia.loans.database.account.loan.query.QDLoan;
import com.ambrosia.loans.discord.message.loan.LoanMessage;
import com.ambrosia.loans.discord.message.loan.LoanMessageBuilder;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import io.ebean.DB;
import io.ebean.Transaction;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

public class LoanFreezeService {

    private static final ScheduledExecutorService SERVICE = Executors.newScheduledThreadPool(1);
    private static ScheduledFuture<?> scheduled = null;

    public static void load() {
        SERVICE.execute(LoanFreezeService::refresh);
    }

    public synchronized static void refresh() {
        if (scheduled != null) scheduled.cancel(false);

        DLoan nextLoan = findNextLoan();
        if (nextLoan == null) {
            Ambrosia.get().logger().info("No future loans to unfreeze. Skipping unfreeze schedule.");
            return;
        }
        Instant unfreezeDate = nextLoan.getMeta().getUnfreezeDate();
        if (unfreezeDate == null) throw new IllegalStateException("UnfreezeDate is null, after being verified");
        Duration delay = Duration.between(Instant.now(), unfreezeDate);
        if (!delay.isPositive()) delay = Duration.ofSeconds(3);

        scheduled = SERVICE.schedule(LoanFreezeService::run, delay.getSeconds(), TimeUnit.SECONDS);
        String timeUntil = "%d days %.2f hours".formatted(delay.toDays(), delay.toMinutes() / 60d);
        Ambrosia.get().logger().info("Scheduled to unfreeze loan{{}} at {} ({}). {} total frozen loans scheduled.",
            nextLoan.getId(), formatDate(unfreezeDate), timeUntil, countNextLoans());
    }

    private static void run() {
        findLoansToUnfreeze().forEach(LoanFreezeService::unfreezeLoan);
//        schedule();
    }

    private static void unfreezeLoan(DLoan loan) {
        DLoanMeta meta = loan.getMeta();
        Double unfreezeToRate = meta.getUnfreezeToRate();
        Instant unfreezeDate = meta.getUnfreezeDate();
        if (unfreezeToRate == null || unfreezeDate == null) {
            String msg = "Error trying to unfreeze loan{%d}. unfreezeToRate or unfreezeDate is null".formatted(loan.getId());
            Ambrosia.get().logger().error(msg);
            return;
        }
        try (Transaction transaction = DB.beginTransaction()) {
            meta.clearUnfreeze();
            loan.save(transaction);
            loan.changeToNewRate(unfreezeToRate, unfreezeDate, transaction);
            transaction.commit();
        }

        EmbedBuilder embed = new EmbedBuilder().setColor(AmbrosiaColor.BLUE_NORMAL);
        LoanMessageBuilder msgBuilder = LoanMessage.of(loan);
        msgBuilder.clientMsg().clientAuthor(embed);
        msgBuilder.loanDescription(embed);

        MessageCreateData message = MessageCreateData.fromEmbeds(embed.build());
        // todo log channel to inform staff
        //      also record messages in db
        Ambrosia.get().logger().info("Sent unfreeze loan message");
        loan.getClient().getDiscord().sendDm(message);
    }

    private static List<DLoan> findLoansToUnfreeze() {
        Timestamp now = Timestamp.from(Instant.now().plusSeconds(1));
        return new QDLoan().where()
            .and()
            .meta.unfreezeDate.isNotNull()
            .meta.unfreezeDate.before(now)
            .orderBy().meta.unfreezeDate.asc()
            .findList();
    }

    @Nullable
    private static DLoan findNextLoan() {
        return queryNextLoan()
            .setMaxRows(1)
            .findOne();
    }

    private static int countNextLoans() {
        return queryNextLoan()
            .findCount();
    }

    private static QDLoan queryNextLoan() {
        return new QDLoan().where()
            .or()
            .meta.unfreezeDate.isNotNull()
            .meta.unfreezeToRate.isNotNull()
            .orderBy().meta.unfreezeDate.asc();
    }
}
