package com.ambrosia.loans.service.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.DLoanMeta;
import com.ambrosia.loans.database.account.loan.query.QDLoan;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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
        double hours = delay.minus(delay.toDays(), ChronoUnit.DAYS)
            .dividedBy(Duration.ofMinutes(1)) / 60d;
        String timeUntil = "%d days %.2f hours".formatted(delay.toDays(), hours);
        Ambrosia.get().logger().info("Scheduled to unfreeze loan{{}} at {} ({}). {} total frozen loans scheduled.",
            nextLoan.getId(), formatDate(unfreezeDate), timeUntil, countNextLoans());
    }

    private static void run() {
        findLoansToUnfreeze().forEach(LoanFreezeService::unfreezeLoan);
        refresh();
    }

    private static void unfreezeLoan(DLoan loan) {
        DLoanMeta meta = loan.getMeta();
        Double unfreezeToRate = meta.getUnfreezeToRate();
        Instant unfreezeDate = meta.getUnfreezeDate();
        if (unfreezeToRate == null || unfreezeDate == null) {
            String msg = "Error trying to unfreeze loan{%d}. unfreezeToRate or unfreezeDate is null".formatted(loan.getId());
            Ambrosia.get().logger().error(msg);
            // todo more logs
            return;
        }
        if (loan.isPaid()) {
            String msg = "Ignoring unfreeze action on loan{%d}. Loan is already paid!".formatted(loan.getId());
            Ambrosia.get().logger().warn(msg);
        }
        loan.unfreezeLoan(unfreezeToRate, unfreezeDate);
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
