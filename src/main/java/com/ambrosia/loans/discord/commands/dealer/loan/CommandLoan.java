package com.ambrosia.loans.discord.commands.dealer.loan;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class CommandLoan extends BaseCommand {

    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
    }

    @Override
    public SlashCommandData getData() {
        return Commands.slash("loan", "Manage loan details").setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of();
    }
}
