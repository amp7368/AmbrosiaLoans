package com.ambrosia.loans.discord.commands.player.profile.page;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;
import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatPercentage;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.DLoanStatus;
import com.ambrosia.loans.database.account.event.loan.payment.DLoanPayment;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.util.emerald.Emeralds;
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
        EmbedBuilder embed = embed("Loans");
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
        embed.addField("Start date", "%s\n".formatted(formatDate(loan.getStartDate())), true);
        String rateMsg = formatPercentage(loan.getLastSection().getRate());
        embed.addField("Rate", rateMsg, true);
        List<String> collateral = loan.getCollateral().stream()
            .map(c -> c.link)
            .toList();
        embed.addField("Collateral", String.join(", ", collateral), true);
        embed.addField("Initial Amount", loan.getInitialAmount().toString(), true);
        embed.addField("Current Balance", loan.getTotalOwed().toString(), true);

        List<DLoanPayment> payments = loan.getPayments();
        if (payments.isEmpty()) {
            embed.setFooter("No Payments made");
            return;
        }
        StringBuilder footer = new StringBuilder("Payments\n");
        for (DLoanPayment payment : payments) {
            footer.append("%s + %s\n".formatted(formatDate(payment.getDate()), payment.getAmount()));
        }
        embed.setFooter(footer.toString());
    }

    private void pastLoansSummary(EmbedBuilder embed) {
        DClient client = getClient();
        if (client.getLoans().isEmpty()) {
            embed.appendDescription("No past loans");
            return;
        }
        List<String> summaries = new ArrayList<>();
        for (DLoan loan : client.getLoans()) {
            if (loan.getStatus() == DLoanStatus.ACTIVE) continue;
            Emeralds initialAmount = loan.getInitialAmount();
            String endDate = formatDate(loan.getEndDate());
            String startDate = formatDate(loan.getStartDate());
            Emeralds totalPaid = loan.getTotalPaid();
            String loanSummary = """
                %s to %s
                Amount: %s
                Total Paid: %s
                """.formatted(startDate, endDate, initialAmount, totalPaid);
            summaries.add(loanSummary);
        }
        embed.appendDescription("## Past Loans\n");

        embed.appendDescription(String.join("\n", summaries));
    }


}
