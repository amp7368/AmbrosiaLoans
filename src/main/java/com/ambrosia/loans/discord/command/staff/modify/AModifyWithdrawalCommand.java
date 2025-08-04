package com.ambrosia.loans.discord.command.staff.modify;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import com.ambrosia.loans.discord.request.base.ModifyRequestMsg;
import com.ambrosia.loans.discord.request.withdrawal.ActiveRequestWithdrawalGui;
import com.ambrosia.loans.discord.request.withdrawal.BaseModifyWithdrawalRequest;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.Nullable;

public class AModifyWithdrawalCommand extends BaseStaffSubCommand implements BaseModifyWithdrawalRequest {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        @Nullable ActiveRequestWithdrawalGui request = findWithdrawalRequest(event, true);
        if (request == null) return;
        List<ModifyRequestMsg> changes = new ArrayList<>();
        changes.add(setAmount(request, event));
        replyChanges(event, changes, request);
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("withdrawal", "[Staff] Modify a withdrawal request");
        return CommandOptionList.of(
            List.of(CommandOption.REQUEST),
            List.of(CommandOption.WITHDRAWAL_AMOUNT)
        ).addToCommand(command);
    }
}
