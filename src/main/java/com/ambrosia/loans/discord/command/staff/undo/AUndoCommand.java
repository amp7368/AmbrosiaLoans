package com.ambrosia.loans.discord.command.staff.undo;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.alter.AlterRecordApi;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterQueryApi;
import com.ambrosia.loans.database.alter.db.DAlterChange;
import com.ambrosia.loans.database.alter.db.DAlterChangeUndoHistory;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffCommand;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class AUndoCommand extends BaseStaffCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DAlterChange record = CommandOption.MODIFICATION_ID.getRequired(event);
        if (record == null) return;
        if (!record.isApplied()) {
            ErrorMessages.alteredAlready("undone").replyError(event);
            return;
        }
        List<DAlterChange> changesSince = AlterQueryApi.findAppliedChangesOnObjAfter(record);
        if (!changesSince.isEmpty()) {
            String changesSinceMessage = changesSince.stream()
                .map(change -> "- Change #" + change.getId())
                .collect(Collectors.joining("\n"));
            String msg = "There have been changes since %s. Undo the following changes first:\n%s"
                .formatted(formatDate(record.getEventDate()), changesSinceMessage);
            replyError(event, msg);
            return;
        }
        DAlterChangeUndoHistory undo = AlterRecordApi.undo(staff, record);
        if (undo == null) {
            String msg = "There was some error undoing %s".formatted(record.getId());
            replyError(event, msg);
            return;
        }
        String msg = "Change %s #%d has been undone".formatted(AmbrosiaEmoji.KEY_ID, undo.getRecord().getId());
        replySuccess(event, msg);
    }

    @Override
    public SlashCommandData getData() {
        SlashCommandData command = Commands.slash("undo", "undo an action");
        CommandOptionList.of(List.of(CommandOption.MODIFICATION_ID))
            .addToCommand(command);
        return command;
    }
}
