package com.ambrosia.loans.discord.commands.player.hisotry;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import com.ambrosia.loans.discord.commands.player.hisotry.loan.LoanHistoryCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class HistoryCommand extends BaseCommand {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new LoanHistoryCommand());
    }

    @Override
    public SlashCommandData getData() {
        return Commands.slash("history", "List past entries");
    }
}
