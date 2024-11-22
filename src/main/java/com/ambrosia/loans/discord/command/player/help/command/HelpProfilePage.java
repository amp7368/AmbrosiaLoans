package com.ambrosia.loans.discord.command.player.help.command;

import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.command.player.help.HelpGuiPage;
import discord.util.dcf.DCFCommandManager;
import discord.util.dcf.gui.base.gui.DCFGui;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class HelpProfilePage extends HelpGuiPage {


    public HelpProfilePage(DCFGui gui) {
        super(gui);
    }

    @Override
    protected MessageEmbed makeEmbed(EmbedBuilder eb) {
        DCFCommandManager commands = DiscordBot.dcf.commands();
        String profile = commands.getCommandAsMention("/profile");
        String account = commands.getCommandAsMention("/request account");
        String loans = commands.getCommandAsMention("/show loans");
        String transactions = commands.getCommandAsMention("/show transactions");
        String collateral = commands.getCommandAsMention("/show collateral");
        eb.addField(profile,
            "View your Ambrosia profile. This will only work if you have an account registered in Ambrosia services.",
            false);
        eb.addField(account + " [minecraft] (display_name)",
            "Register an account linked with your discord and minecraft.",
            false);
        eb.addField(loans,
            "View detailed information about your borrowing history.",
            false);
        eb.addField(transactions,
            "View detailed information about past transactions.",
            false);
        eb.addField(collateral,
            "View detailed information about loan collateral.",
            false);
        return eb.build();
    }

    @Override
    protected String getTitle() {
        return "Profile Commands";
    }

}
