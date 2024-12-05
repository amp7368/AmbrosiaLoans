package com.ambrosia.loans.discord.command.staff.find.loan;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.message.loan.LoanCollateralPage;
import com.ambrosia.loans.discord.message.loan.LoanMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import discord.util.dcf.gui.base.gui.DCFGui;
import discord.util.dcf.gui.base.page.DCFGuiPage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class FindLoanMessage extends DCFGuiPage<DCFGui> implements LoanMessage, ClientMessage {

    private final DLoan loan;

    public FindLoanMessage(DCFGui parent, DLoan loan) {
        super(parent);
        this.loan = loan;
        registerButton(LoanCollateralPage.showCollateralBtnId(), event -> {
            LoanCollateralPage page = new LoanCollateralPage(parent, loan, true);
            parent.addSubPage(page);
        });
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setColor(AmbrosiaColor.BLUE_NORMAL);

        clientAuthor(embed);
        loanDescription(embed);

        Button collateralBtn = LoanCollateralPage.showCollateralBtn(false);
        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .setComponents(ActionRow.of(collateralBtn))
            .build();
    }

    @Override
    public boolean includeRequestDetails() {
        return true;
    }

    @Override
    public DLoan getLoan() {
        return loan;
    }

    @Override
    public DClient getClient() {
        return loan.getClient();
    }
}
