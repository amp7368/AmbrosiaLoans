package com.ambrosia.loans.discord.message.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;
import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatPercentage;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.DLoanStatus;
import com.ambrosia.loans.database.account.loan.section.DLoanSection;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;

public interface LoanMessage {

    static LoanMessageBuilder of(DLoan loan) {
        return new LoanMessageBuilder(loan);
    }

    DLoan getLoan();

    default boolean includeHistory() {
        return true;
    }

    default void loanDescription(EmbedBuilder embed) {
        DLoan loan = getLoan();
        DLoanStatus status = loan.getStatus();
        AmbrosiaEmoji statusEmoji = status.getEmoji();
        embed.appendDescription("### Loan %s %d - %s %s\n".formatted(AmbrosiaEmoji.KEY_ID, loan.getId(), statusEmoji, status));

        embed.appendDescription("**%s Start:** %s\n".formatted(AmbrosiaEmoji.ANY_DATE, formatDate(loan.getStartDate())));
        String rateMsg = formatPercentage(loan.getLastSection().getRate());
        embed.appendDescription("**%s Current Rate:** %s\n".formatted(AmbrosiaEmoji.LOAN_RATE, rateMsg));
        embed.appendDescription("**%s Initial Amount:** %s\n".formatted(AmbrosiaEmoji.LOAN_BALANCE, loan.getInitialAmount()));
        String collateral = loan.getCollateral().stream()
            .map(c -> c.link)
            .collect(Collectors.joining(", "));
        String collateralMsg = collateral.isBlank() ? "None" : collateral;
        embed.appendDescription("%s **Collateral:** %s\n".formatted(AmbrosiaEmoji.LOAN_COLLATERAL, collateralMsg));

        if (!includeHistory()) return;

        List<LoanEventMsg> history = new ArrayList<>();
        findChangeRateEvents(loan, history);
        findPaymentEvents(loan, history);
        embed.appendDescription("## History\n");

        String historyMsg = history.stream()
            .map(LoanEventMsg::toString)
            .collect(Collectors.joining("\n"));
        embed.appendDescription(historyMsg);
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
            String id = AmbrosiaEmoji.KEY_ID.spaced(payment.getId());
            String msg = "Payment %s %s".formatted(payment.getAmount(), id);
            history.add(new LoanEventMsg(AmbrosiaEmoji.LOAN_PAYMENT, msg, payment.getDate()));
        }
    }

    record LoanEventMsg(AmbrosiaEmoji emoji, String msg, Instant date) {

        @Override
        public String toString() {
            return "%s %s %s %s".formatted(emoji, msg, AmbrosiaEmoji.ANY_DATE, formatDate(date));
        }
    }
}
