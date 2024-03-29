package com.ambrosia.loans.discord.command.staff.profile;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ACommandLink extends BaseCommand {

    @Override
    public SlashCommandData getData() {
        SlashCommandData command = Commands.slash("alink", "Link minecraft and/or discord");
        return command.setDefaultPermissions(DefaultMemberPermissions.DISABLED).setGuildOnly(true);
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new CommandLinkDiscord(), new CommandLinkMinecraft());
    }

    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
    }
}
