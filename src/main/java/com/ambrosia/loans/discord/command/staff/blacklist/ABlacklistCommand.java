package com.ambrosia.loans.discord.command.staff.blacklist;

import com.ambrosia.loans.discord.base.command.staff.BaseStaffCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ABlacklistCommand extends BaseStaffCommand {

    @Override
    public SlashCommandData getStaffData() {
        return Commands.slash("ablacklist", "[Staff] Blacklist related commands");
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(
            new BlacklistSetCommand("add", true),
            new BlacklistSetCommand("remove", false),
            new BlacklistListCommand()
        );
    }
}
