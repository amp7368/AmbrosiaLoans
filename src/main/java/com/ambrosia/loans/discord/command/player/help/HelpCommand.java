package com.ambrosia.loans.discord.command.player.help;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import com.ambrosia.loans.discord.command.player.help.command.HelpInvestmentPage;
import com.ambrosia.loans.discord.command.player.help.command.HelpLoanPage;
import com.ambrosia.loans.discord.command.player.help.command.HelpProfilePage;
import discord.util.dcf.gui.base.gui.DCFGui;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public class HelpCommand extends BaseCommand {

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        helpGUI(event).send();
    }

    public @NotNull DCFGui helpGUI(SlashCommandInteractionEvent event) {
        DCFGui gui = new DCFGui(dcf, event::reply);
        // commands manual
        gui.addPage(new HelpProfilePage(gui),
            new HelpLoanPage(gui),
            new HelpInvestmentPage(gui)
        );
        return gui;
    }

    @Override
    public SlashCommandData getData() {
        return Commands.slash("help", "Helpful information");
    }
}
