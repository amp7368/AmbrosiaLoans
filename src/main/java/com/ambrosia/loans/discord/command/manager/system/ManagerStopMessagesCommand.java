package com.ambrosia.loans.discord.command.manager.system;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.option.StopStartAction;
import com.ambrosia.loans.discord.base.command.staff.BaseManagerSubCommand;
import com.ambrosia.loans.service.message.MessageManager;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ManagerStopMessagesCommand extends BaseManagerSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        StopStartAction action = CommandOption.STOP_START.getRequired(event);
        if (action == null) return;
        if (action == StopStartAction.STOP)
            MessageManager.stop();
        else MessageManager.start();

        String msg = "Sent '%s' command to messaging services."
            .formatted(action.name().toLowerCase());

        replySuccess(event, msg);
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("messaging",
            "[Manager] Start/stop all messaging services.");
        return CommandOptionList.of(
            List.of(CommandOption.STOP_START)
        ).addToCommand(command);
    }
}
