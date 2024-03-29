package com.ambrosia.loans.discord.command.player.help;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import discord.util.dcf.gui.base.gui.DCFGui;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class HelpCommand extends BaseCommand {

    @Override
    public SlashCommandData getData() {
        return Commands.slash("help", "Various help commands");
    }

    @Override
    public boolean isOnlyManager() {
        return true;
    }

    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
        DCFGui gui = new DCFGui(dcf, event::reply);
        gui.addPage(new HelpHomePage(gui));
        gui.addPage(new HelpCommandsPage(gui));
        gui.send();
    }
}
