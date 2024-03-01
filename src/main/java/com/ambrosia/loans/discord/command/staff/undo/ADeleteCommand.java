package com.ambrosia.loans.discord.command.staff.undo;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.LoanApi.LoanQueryApi;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterCreateApi;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterQueryApi;
import com.ambrosia.loans.database.alter.change.DAlterChange;
import com.ambrosia.loans.database.alter.create.DAlterCreate;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffCommand;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ADeleteCommand extends BaseStaffCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        AlterCreateType entityType = CommandOption.DELETE_ENTITY_TYPE.getRequired(event);
        if (entityType == null) return;
        Long entityId = CommandOption.DELETE_ENTITY_ID.getRequired(event);
        if (entityId == null) return;

        DAlterCreate create = AlterQueryApi.findCreateByEntityId(entityId, entityType.getTypeId());
        if (create == null) {
            replyError(event, "That %s does not exist in the database".formatted(entityType.displayName()));
            return;
        }
        List<DAlterChange> appliedChanges = create.getAppliedChanges();
        if (!appliedChanges.isEmpty()) {
            String changesSinceMessage = appliedChanges.stream()
                .map(change -> "- Change #" + change.getId())
                .collect(Collectors.joining("\n"));
            String msg = "There have been changes since this %s has been created. Undo the following changes first:\n%s"
                .formatted(entityType.displayName(), changesSinceMessage);
            replyError(event, msg);
            return;
        }
        if (entityType == AlterCreateType.LOAN) {
            DLoan loan = LoanQueryApi.findById(entityId);
            if (loan == null || !loan.getPayments().isEmpty()) {
                replyError(event, "Loan has payments that have been made!");
                return;
            }
        }
        try {
            AlterCreateApi.delete(staff, create);
            replySuccess(event, "Deleted %s %s %d".formatted(entityType.displayName(), AmbrosiaEmoji.KEY_ID, entityId));
        } catch (Exception e) {
            replyError(event, "Error deleting %s %s %d".formatted(entityType.displayName(), AmbrosiaEmoji.KEY_ID, entityId));
        }
    }

    @Override
    public SlashCommandData getData() {
        SlashCommandData command = Commands.slash("adelete", "Delete a database entity");
        CommandOptionList.of(
            List.of(CommandOption.DELETE_ENTITY_TYPE, CommandOption.DELETE_ENTITY_ID)
        ).addToCommand(command);
        return command;
    }
}
