package com.ambrosia.loans.discord.command.manager.config.todo;

import com.ambrosia.loans.discord.base.command.staff.BaseManagerSubCommandGroup;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public class StaffTodoCommand extends BaseManagerSubCommandGroup {

    @Override
    public SubcommandGroupData getData() {
        return new SubcommandGroupData("todo", "[Manager] Todo list actions");
    }

    @Override
    protected List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new StaffTodoListCommand());
    }
}
