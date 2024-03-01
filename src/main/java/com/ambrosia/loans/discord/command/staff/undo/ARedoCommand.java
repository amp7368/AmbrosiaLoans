package com.ambrosia.loans.discord.command.staff.undo;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.alter.AlterRecordApi;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterQueryApi;
import com.ambrosia.loans.database.alter.change.DAlterChange;
import com.ambrosia.loans.database.alter.change.DAlterChangeUndoHistory;
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

public class ARedoCommand extends BaseStaffCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DAlterChange record = CommandOption.MODIFICATION_ID.getRequired(event);
        if (record == null) return;
        if (record.isApplied()) {
            ErrorMessages.alteredAlready("applied").replyError(event);
            return;
        }
        List<DAlterChange> changesBefore = AlterQueryApi.findUnAppliedChangesBefore(record);
        if (!changesBefore.isEmpty()) {
            String changesBeforeMsg = changesBefore.stream().map(change -> "- Change #" + change.getId())
                .collect(Collectors.joining("\n"));
            String msg = "There are unapplied changes made before %s.\nRedo the following changes first:\n%s".formatted(
                formatDate(record.getEventDate()), changesBeforeMsg);
            replyError(event, msg);
            return;
        }
        DAlterChangeUndoHistory redo = AlterRecordApi.redo(staff, record);
        if (redo == null) {
            String msg = "There was some error redoing %s".formatted(record.getId());
            replyError(event, msg);
            return;
        }
        String msg = "Change %s #%d has been reapplied".formatted(AmbrosiaEmoji.KEY_ID, redo.getRecord().getId());
        replySuccess(event, msg);
    }

    @Override
    public SlashCommandData getData() {
        SlashCommandData command = Commands.slash("redo", "redo an action");
        CommandOptionList.of(List.of(CommandOption.MODIFICATION_ID))
            .addToCommand(command);
        return command;
    }
}
