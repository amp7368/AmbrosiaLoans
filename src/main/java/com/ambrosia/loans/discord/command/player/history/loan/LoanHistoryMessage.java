package com.ambrosia.loans.discord.command.player.history.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;
import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatPercentage;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.DLoanStatus;
import com.ambrosia.loans.database.account.loan.section.DLoanSection;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.SendMessageClient;
import com.ambrosia.loans.discord.base.gui.DCFScrollGuiFixed;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import discord.util.dcf.gui.scroll.DCFEntry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class LoanHistoryMessage extends DCFScrollGuiFixed<ClientGui, DLoan> implements SendMessageClient {

    public LoanHistoryMessage(ClientGui parent) {
        super(parent);
        setEntries(getClient().getLoans());
        sort();
    }


    @Override
    public DClient getClient() {
        return parent.getClient();
    }

    @Override
    protected Comparator<? super DLoan> entriesComparator() {
        return Comparator.comparing(DLoan::getStartDate).reversed();
    }

    @Override
    protected int entriesPerPage() {
        return 1;
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(AmbrosiaColor.BLUE_NORMAL);
        embed.setTitle(title("Loan History", entryPage, getMaxPage()));
        author(embed);

        List<DCFEntry<DLoan>> page = getCurrentPageEntries();
        if (page.isEmpty()) {
            embed.setDescription("### No Loan History");
            return makeMessage(embed.build());
        }
        DLoan loan = page.get(0).entry();

        DLoanStatus status = loan.getStatus();
        AmbrosiaEmoji statusEmoji = status.getEmoji();
        embed.appendDescription("### Loan [#%d] - %s %s\n".formatted(loan.getId(), statusEmoji, status));

        embed.appendDescription("**%s Start:** %s\n".formatted(AmbrosiaEmoji.ANY_DATE, formatDate(loan.getStartDate())));
        String rateMsg = formatPercentage(loan.getLastSection().getRate());
        embed.appendDescription("**%s Current Rate:** %s\n".formatted(AmbrosiaEmoji.LOAN_RATE, rateMsg));
        embed.appendDescription("**%s Initial Amount:** %s\n".formatted(AmbrosiaEmoji.LOAN_BALANCE, loan.getInitialAmount()));
        String collateral = loan.getCollateral().stream()
            .map(c -> c.link)
            .collect(Collectors.joining(", "));
        String collateralMsg = collateral.isBlank() ? "None" : collateral;
        embed.appendDescription("%s **Collateral:** %s\n".formatted(AmbrosiaEmoji.LOAN_COLLATERAL, collateralMsg));

        List<LoanEventMsg> history = new ArrayList<>();
        findChangeRateEvents(loan, history);
        findPaymentEvents(loan, history);
        embed.appendDescription("## History\n");
        String historyMsg = history.stream()
            .map(LoanEventMsg::toString)
            .collect(Collectors.joining("\n"));
        embed.appendDescription(historyMsg);

        return makeMessage(embed.build());
    }

    private void findChangeRateEvents(DLoan loan, List<LoanEventMsg> history) {
        List<DLoanSection> sections = loan.getSections();
        if (sections.size() <= 1) return;

        DLoanSection section1 = sections.get(0);
        String msg1 = "Rate started at " + formatPercentage(section1.getRate());
        history.add(new LoanEventMsg(AmbrosiaEmoji.LOAN_RATE, msg1, section1.getStartDate()));

        double lastRate = section1.getRate();
        for (DLoanSection section : sections) {
            if (lastRate == section.getRate()) continue;
            String msg = "Rate change: %s => %s "
                .formatted(formatPercentage(lastRate),
                    formatPercentage(section.getRate()));
            lastRate = section.getRate();
            history.add(new LoanEventMsg(AmbrosiaEmoji.LOAN_RATE, msg, section.getStartDate()));
        }
    }

    private void findPaymentEvents(DLoan loan, List<LoanEventMsg> history) {
        for (DLoanPayment payment : loan.getPayments()) {
            String msg = "Payment made: " + payment.getAmount();
            history.add(new LoanEventMsg(AmbrosiaEmoji.LOAN_PAYMENT, msg, payment.getDate()));
        }
    }

    private MessageCreateData makeMessage(MessageEmbed embed) {
        return new MessageCreateBuilder()
            .setEmbeds(embed)
            .setComponents(ActionRow.of(btnFirst(), btnNext(), btnPrev()))
            .build();
    }

    private record LoanEventMsg(AmbrosiaEmoji emoji, String msg, Instant date) {

        @Override
        public String toString() {
            return "%s %s - %s".formatted(emoji, formatDate(date), msg);
        }
    }
}
