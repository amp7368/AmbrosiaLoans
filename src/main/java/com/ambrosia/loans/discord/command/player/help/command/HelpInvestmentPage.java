package com.ambrosia.loans.discord.command.player.help.command;

import com.ambrosia.loans.config.AmbrosiaConfig;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.command.player.help.HelpGuiPage;
import discord.util.dcf.DCFCommandManager;
import discord.util.dcf.gui.base.gui.DCFGui;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class HelpInvestmentPage extends HelpGuiPage {


    public HelpInvestmentPage(DCFGui gui) {
        super(gui);
    }

    @Override
    protected MessageEmbed makeEmbed(EmbedBuilder eb) {
        DCFCommandManager commands = DiscordBot.dcf.commands();

        String hyperlink = AmbrosiaConfig.staff().getCurrentTOS().hyperlink();
        String requestInvestment = commands.getCommandAsMention("/request investment");
        String requestWithdrawal = commands.getCommandAsMention("/request withdrawal");
        String modifyRequest = commands.getCommandAsMention("/modify_request investment");
        String modifyWithdrawal = commands.getCommandAsMention("/modify_request withdrawal");

        eb.addField(requestInvestment + " [amount]",
            "Request to invest with Ambrosia Loans or to add to your current investment. Specify an amount with units like \"23 STX "
                + "12 LE 8 EB 56 E\" or \"12.75 STX\". Please review our " + hyperlink
                + " for more information, or DM inquiries to @tealycraft.",
            false);
        eb.addField(requestWithdrawal + " (amount) (full)",
            "Request to withdraw from your investment. Specify an amount Specify an amount with units like \"23 STX 12 LE 8 EB 56 "
                + "E\" or \"12.75 STX\" or in full to withdraw your entire "
                + "balance. Partial withdrawals will pull out of your investor profits first, then investment. Please review our "
                + hyperlink + " for rules regarding withdrawing, and DM any inquiries to @tealycraft.",
            false);
        eb.addField("%s [request_id]\n%s [request_id]".formatted(modifyRequest, modifyWithdrawal),
            "Modify a previous request, such as changing the amount of the investment/withdrawal. The request ID can be found in "
                + "the bot message for each request.",
            false);
        return eb.build();
    }

    @Override
    protected String getTitle() {
        return "Investor Commands";
    }

}
