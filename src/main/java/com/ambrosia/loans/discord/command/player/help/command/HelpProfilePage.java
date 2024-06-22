package com.ambrosia.loans.discord.command.player.help.command;

import com.ambrosia.loans.discord.command.player.help.HelpGuiPage;
import discord.util.dcf.gui.base.gui.DCFGui;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class HelpProfilePage extends HelpGuiPage {


    public HelpProfilePage(DCFGui gui) {
        super(gui);
    }

    @Override
    protected MessageEmbed makeEmbed(EmbedBuilder eb) {
        eb.addField("/profile",
            "View your Ambrosia profile. This will only work if you have an account registered in Ambrosia services.",
            false);
        eb.addField("/request account [minecraft] (display_name)",
            "Register an account linked with your discord and minecraft.",
            false);
        eb.addField("/history loans",
            "View detailed information about your borrowing history.",
            false);
        eb.addField("/history transactions",
            "View detailed information about past transactions.",
            false);
        return eb.build();
    }

    @Override
    protected String getTitle() {
        return "Profile Commands";
    }

}
