package com.ambrosia.loans.discord.command.player.profile.page;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.gui.ClientGui;
import com.ambrosia.loans.discord.message.loan.LoanCollateralPage;
import com.ambrosia.loans.discord.message.loan.LoanMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class ProfileLoanPage extends ProfilePage {


    public ProfileLoanPage(ClientGui parent) {
        super(parent);
        registerButton(LoanCollateralPage.showCollateralBtnId(), e -> {
            Optional<DLoan> loan = getClient().getActiveLoan();
            if (loan.isEmpty()) return;
            parent.addSubPage(new LoanCollateralPage(parent, loan.get(), true));
        });
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = embed("Loans", AmbrosiaColor.BLUE_NORMAL);
        balance(embed);
        pastLoansSummary(embed);

        activeLoan(embed);

        Button collateralBtn = LoanCollateralPage.showCollateralBtn(getClient().getActiveLoan().isEmpty());
        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .setComponents(
                ActionRow.of(pageBtns()),
                ActionRow.of(collateralBtn)
            )
            .build();
    }

    private void activeLoan(EmbedBuilder embed) {
        Optional<DLoan> activeLoan = getClient().getActiveLoan();
        if (activeLoan.isEmpty()) {
            embed.appendDescription("### No Active Loans\n");
            return;
        }

        LoanMessage.of(activeLoan.get()).loanDescription(embed);
    }

    private void pastLoansSummary(EmbedBuilder embed) {
        DClient client = getClient();
        List<DLoan> loans = client.getLoans().stream()
            .sorted(Comparator.comparing(DLoan::getStartDate).reversed())
            .toList();
        if (loans.isEmpty()) {
            embed.appendDescription("## No past loans");
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
            String idLine = "**Loan:** " + AmbrosiaEmoji.KEY_ID.spaced(loan.getId());
            String startDate = formatDate(loan.getStartDate());
            String endDate = formatDate(loan.getEndDate());
            String line1 = "%s **Timespan:** %s to %s".formatted(AmbrosiaEmoji.ANY_DATE, startDate, endDate);
            String line2 = "%s **Amount:** %s".formatted(AmbrosiaEmoji.LOAN_BALANCE, loan.getInitialAmount());
            String line3 = "%s **Total Paid:** %s\n".formatted(AmbrosiaEmoji.LOAN_PAYMENT, loan.getTotalPaid());

            summaries.add(String.join("\n", idLine, line1, line2, line3));
        }
        embed.appendDescription("## Past Loans\n");

        embed.appendDescription(String.join("\n", summaries));
        if (hitMaxLoans)
            embed.appendDescription("...\n");
    }
}
