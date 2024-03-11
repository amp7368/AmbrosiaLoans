package com.ambrosia.loans.discord.command.staff.alter.withdrawal;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class AWithdrawalSetCommand extends BaseCommand {

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new WithdrawalMakeCommand());
    }

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
    }

    @Override
    public SlashCommandData getData() {
        return Commands.slash("awithdrawal", "Modify anything about a withdrawal");
    }
}
