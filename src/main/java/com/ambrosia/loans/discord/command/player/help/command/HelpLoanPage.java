package com.ambrosia.loans.discord.command.player.help.command;

import com.ambrosia.loans.config.AmbrosiaConfig;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.command.player.help.HelpGuiPage;
import discord.util.dcf.DCFCommandManager;
import discord.util.dcf.gui.base.gui.DCFGui;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class HelpLoanPage extends HelpGuiPage {

    public HelpLoanPage(DCFGui gui) {
        super(gui);
    }

    @Override
    protected MessageEmbed makeEmbed(EmbedBuilder eb) {
        DCFCommandManager commands = DiscordBot.dcf.commands();

        String hyperlink = AmbrosiaConfig.staff().getCurrentTOS().hyperlink();

        String requestLoan = commands.getCommandAsMention("/request loan");
        String collateralAdd = commands.getCommandAsMention("/collateral add");
        String requestPayment = commands.getCommandAsMention("/request payment");
        String modifyRequest = commands.getCommandAsMention("/modify_request loan");
        eb.addField(requestLoan,
            "Open a form to apply for a new loan. In the form, specify an amount with units like \"23 STX 12 LE 8 EB 56 E\" or \"12"
                + ".75 STX\". Please review our " + hyperlink
                + " for more information, or DM inquiries to @tealycraft.",
            false);
        eb.addField(collateralAdd,
            "**After** completing the loan form, add collateral to complete the loan application.",
            false);
        eb.addField(requestPayment + " (amount) (full)",
            "Request to make a payment on your active loan. Specify an amount for a partial payment, or full to pay the full balance.",
            false);
        eb.addField(modifyRequest + " (request_id)",
            "Modify a previous request, such as adding a Voucher or Reputable Vouch to a loan. The optional request ID can be found "
                + "in the "
                + "bot message for each request.",
            false);

        return eb.build();
    }

    @Override
    protected String getTitle() {
        return "Loan Commands";
    }

}
