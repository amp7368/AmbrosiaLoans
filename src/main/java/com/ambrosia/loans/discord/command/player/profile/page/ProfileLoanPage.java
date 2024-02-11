package com.ambrosia.loans.discord.command.player.profile.page;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;
import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatPercentage;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.payment.DLoanPayment;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class ProfileLoanPage extends ProfilePage {


    public ProfileLoanPage(ClientGui parent) {
        super(parent);
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = embed("Loans", AmbrosiaColor.BLUE_NORMAL);
        balance(embed);
        pastLoansSummary(embed);

        activeLoan(embed);

        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .setComponents(ActionRow.of(pageBtns()))
            .build();
    }

    private void activeLoan(EmbedBuilder embed) {
        Optional<DLoan> activeLoan = getClient().getActiveLoan();
        if (activeLoan.isEmpty()) {
            embed.appendDescription("### No Active Loans\n");
            return;
        }
        DLoan loan = activeLoan.get();
        embed.appendDescription("### Active Loan [#%s]\n".formatted(loan.getId()));
        embed.addField("Start date", "%s\n".formatted(formatDate(loan.getStartDate(), true)), true);
        String rateMsg = AmbrosiaEmoji.LOAN_RATE.spaced() + formatPercentage(loan.getLastSection().getRate());
        embed.addField("Rate", rateMsg, true);
        List<String> collateral = loan.getCollateral().stream()
            .map(c -> c.link)
            .toList();
        String collateralMsg = AmbrosiaEmoji.COLLATERAL.spaced() + String.join("\n", collateral);
        embed.addField("Collateral", collateralMsg, true);
        String initialAmountMsg = AmbrosiaEmoji.LOAN_BALANCE.spaced() + loan.getInitialAmount();
        embed.addField("Initial Amount", initialAmountMsg, true);
        String currentBalanceMsg = AmbrosiaEmoji.LOAN_BALANCE.spaced() + loan.getTotalOwed();
        embed.addField("Current Balance", currentBalanceMsg, true);
        String interestMsg = AmbrosiaEmoji.INTEREST.spaced() + loan.getAccumulatedInterest();
        embed.addField("Total Interest", interestMsg, true);

        List<DLoanPayment> payments = loan.getPayments();
        if (payments.isEmpty()) {
            embed.setFooter("No Payments made");
            return;
        }
        StringBuilder footer = new StringBuilder("Payments\n");
        for (DLoanPayment payment : payments) {
            String entry = "%s + %s\n".formatted(formatDate(payment.getDate()), payment.getAmount());
            footer.append(entry);
        }
        embed.setFooter(footer.toString());
    }

    private void pastLoansSummary(EmbedBuilder embed) {
        DClient client = getClient();
        List<DLoan> loans = client.getLoans();
        if (loans.isEmpty()) {
            embed.appendDescription("No past loans");
            return;
        }
        List<String> summaries = new ArrayList<>();
        int MAX_LISTED_LOANS = 4;
        boolean hitMaxLoans = false;
        for (int i = 0; i < loans.size(); i++) {
            DLoan loan = loans.get(i);
            if (loan.getStatus().isActive()) continue;
            if (i >= MAX_LISTED_LOANS) {
                hitMaxLoans = true;
                break;
            }
            String endDate = formatDate(loan.getEndDate());
            String startDate = formatDate(loan.getStartDate(), true);
            String line1 = "%s to %s\n".formatted(startDate, endDate);
            String line2 = "%s Amount: %s\n".formatted(AmbrosiaEmoji.LOAN_BALANCE, loan.getInitialAmount());
            String line3 = "%s Total Paid: %s\n".formatted(AmbrosiaEmoji.LOAN_PAYMENT, loan.getTotalPaid());

            summaries.add(line1 + line2 + line3);
        }
        embed.appendDescription("### Past Loans\n");

        embed.appendDescription(String.join("\n", summaries));
        if (hitMaxLoans)
            embed.appendDescription("...\n");
    }
}
