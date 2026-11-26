package com.ambrosia.loans.discord.command.manager.config;

import com.ambrosia.loans.discord.base.command.staff.BaseManagerCommand;
import com.ambrosia.loans.discord.command.manager.config.todo.StaffTodoCommand;
import com.ambrosia.loans.discord.command.manager.config.tos.StaffConfigTOSCommandGroup;
import discord.util.dcf.slash.DCFSlashSubCommandGroup;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class StaffConfigCommand extends BaseManagerCommand {

    @Override
    protected List<DCFSlashSubCommandGroup> getSubCommandGroups() {
        return List.of(new StaffConfigTOSCommandGroup(), new StaffTodoCommand());
    }

    @Override
    public SlashCommandData getStaffData() {
        return Commands.slash("config", "[Manager] Manage/update the staff config");
    }
}
