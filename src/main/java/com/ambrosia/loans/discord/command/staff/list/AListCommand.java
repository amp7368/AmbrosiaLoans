package com.ambrosia.loans.discord.command.staff.list;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import com.ambrosia.loans.discord.command.staff.list.client.ListClientsCommand;
import com.ambrosia.loans.discord.command.staff.list.loan.ListLoansCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class AListCommand extends BaseCommand {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new ListLoansCommand(), new ListClientsCommand());
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SlashCommandData getData() {
        return Commands.slash("alist", "List data commands");
    }
}
