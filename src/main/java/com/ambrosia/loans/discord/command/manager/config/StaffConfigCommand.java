package com.ambrosia.loans.discord.command.manager.config;

import com.ambrosia.loans.discord.base.command.staff.BaseManagerCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class StaffConfigCommand extends BaseManagerCommand {

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new StaffConfigTOSCommand());
    }

    @Override
    public SlashCommandData getStaffData() {
        return Commands.slash("config", "[Manager] Manage/update the staff config");
    }
}
