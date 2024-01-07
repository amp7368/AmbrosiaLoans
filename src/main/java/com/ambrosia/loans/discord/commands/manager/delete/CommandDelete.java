package com.ambrosia.loans.discord.commands.manager.delete;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class CommandDelete extends BaseCommand {

    @Override
    public SlashCommandData getData() {
        SlashCommandData command = Commands.slash("delete", "Delete a log entry or profile");
        return command.setDefaultPermissions(DefaultMemberPermissions.DISABLED).setGuildOnly(true);
    }

    @Override
    public boolean isOnlyManager() {
        return true;
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new CommandDeleteProfile());
    }

    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
    }
}
