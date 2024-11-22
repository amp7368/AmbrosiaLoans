package com.ambrosia.loans.discord.command.staff.profile;

import com.ambrosia.loans.discord.base.command.staff.BaseStaffCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ACommandLink extends BaseStaffCommand {

    @Override
    public SlashCommandData getStaffData() {
        SlashCommandData command = Commands.slash("alink", "[Staff] Link minecraft and/or discord");
        return command.setDefaultPermissions(DefaultMemberPermissions.DISABLED).setGuildOnly(true);
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new CommandLinkDiscord(), new CommandLinkMinecraft());
    }
}
