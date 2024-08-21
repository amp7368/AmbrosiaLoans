package com.ambrosia.loans.discord.command.player.help.command;

import com.ambrosia.loans.config.AmbrosiaConfig;
import com.ambrosia.loans.discord.command.player.help.HelpGuiPage;
import discord.util.dcf.gui.base.gui.DCFGui;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class HelpLoanPage extends HelpGuiPage {

    public HelpLoanPage(DCFGui gui) {
        super(gui);
    }

    @Override
    protected MessageEmbed makeEmbed(EmbedBuilder eb) {
        String hyperlink = AmbrosiaConfig.staff().getCurrentTOS().hyperlink();
        eb.addField("/request loan",
            "Open a form to apply for a new loan. In the form, specify an amount with units like \"23 STX 12 LE 8 EB 56 E\" or \"12"
                + ".75 STX\". Please review our " + hyperlink
                + " for more information, or DM inquiries to @tealycraft.",
            false);
        eb.addField("/collateral add",
            "**After** completing the loan form, add collateral to complete the loan application.",
            false);
        eb.addField("/request payment (amount) (full)",
            "Request to make a payment on your active loan. Specify an amount for a partial payment, or full to pay the full balance.",
            false);
        eb.addField("/modify request loan [request_id]",
            "Modify a previous request, such as adding a Voucher or Reputable Vouch to a loan. The request ID can be found in the "
                + "bot message for each request.",
            false);

        return eb.build();
    }

    @Override
    protected String getTitle() {
        return "Loan Commands";
    }

}
