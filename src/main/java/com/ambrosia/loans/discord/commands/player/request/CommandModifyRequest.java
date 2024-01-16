package com.ambrosia.loans.discord.commands.player.request;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import com.ambrosia.loans.discord.commands.player.request.loan.CommandModifyLoan;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class CommandModifyRequest extends BaseCommand {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {

    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new CommandModifyLoan());
    }

    @Override
    public SlashCommandData getData() {
        return Commands.slash("modify_request", "Modify a request");
    }

}
