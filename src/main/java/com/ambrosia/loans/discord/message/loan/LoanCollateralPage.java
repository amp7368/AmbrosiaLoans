package com.ambrosia.loans.discord.message.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.DLoanStatus;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import discord.util.dcf.gui.base.gui.DCFGui;
import discord.util.dcf.gui.base.page.DCFGuiPage;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

public class LoanCollateralPage extends DCFGuiPage<DCFGui> {

    private final DLoan loan;

    public LoanCollateralPage(DCFGui parent, DLoan loan) {
        super(parent);
        this.loan = loan;
        registerButton(btnBack().getId(), e -> parent.popSubPage());
    }

    public static String showCollateralBtnId() {
        return "show_collateral";
    }

    public static @NotNull Button showCollateralBtn(boolean disabled) {
        return Button.secondary(showCollateralBtnId(), "Show Collateral")
            .withDisabled(disabled);
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        DLoanStatus status = loan.getStatus();
        AmbrosiaEmoji statusEmoji = status.getEmoji();
        embed.appendDescription(
            "## Loan Collateral %s %d - %s %s\n".formatted(AmbrosiaEmoji.KEY_ID, loan.getId(), statusEmoji, status));

        embed.appendDescription("### %s Start: %s\n".formatted(AmbrosiaEmoji.ANY_DATE, formatDate(loan.getStartDate())));

        String collateral = loan.getCollateral().stream()
            .map(c -> "\n" + AmbrosiaEmoji.LOAN_COLLATERAL.spaced(c.link))
            .collect(Collectors.joining(", "));
        String collateralMsg = collateral.isBlank() ? "None" : collateral;
        embed.appendDescription(collateralMsg + "\n");

        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .setComponents(ActionRow.of(btnBack()))
            .build();
    }

    private Button btnBack() {
        return Button.danger("back_page", "Back");
    }
}
