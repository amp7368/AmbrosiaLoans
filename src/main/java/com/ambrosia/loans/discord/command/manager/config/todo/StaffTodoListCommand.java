package com.ambrosia.loans.discord.command.manager.config.todo;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.staff.BaseManagerSubCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class StaffTodoListCommand extends BaseManagerSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        replySuccess(event, "Hello world!");
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("list", "[Manager] Show the todo list.");
    }
}
