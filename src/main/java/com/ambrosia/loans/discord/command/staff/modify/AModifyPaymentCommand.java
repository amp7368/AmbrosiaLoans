package com.ambrosia.loans.discord.command.staff.modify;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import com.ambrosia.loans.discord.request.base.ModifyRequestMsg;
import com.ambrosia.loans.discord.request.payment.ActiveRequestPaymentGui;
import com.ambrosia.loans.discord.request.payment.BaseModifyPaymentRequest;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.Nullable;

public class AModifyPaymentCommand extends BaseStaffSubCommand implements BaseModifyPaymentRequest {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        @Nullable ActiveRequestPaymentGui request = findPaymentRequest(event, true);
        if (request == null) return;
        List<ModifyRequestMsg> changes = new ArrayList<>();
        changes.add(setPaymentAmount(request, event));
        changes.add(setDate(request, event));
        replyChanges(event, changes, request);
    }

    private ModifyRequestMsg setPaymentAmount(ActiveRequestPaymentGui request, SlashCommandInteractionEvent event) {
        Emeralds amount = CommandOption.PAYMENT_AMOUNT.getOptional(event);
        if (amount == null) return null;

        request.getData().setPayment(amount);
        String msg = "Set the payment amount to %s".formatted(amount);
        return ModifyRequestMsg.info(msg);
    }

    private ModifyRequestMsg setDate(ActiveRequestPaymentGui request, SlashCommandInteractionEvent event) {
        Instant date = CommandOption.DATE.getOptional(event);
        if (date == null) return null;
        try {
            request.getData().setTimestamp(date);
            String msg = "Set the date to %s".formatted(formatDate(date));
            return ModifyRequestMsg.info(msg);
        } catch (DateTimeParseException e) {
            String dateString = CommandOption.DATE.getMap1(event);
            String msg = "Failed to parse date '%s'. Please use format MM/DD/YY".formatted(dateString);
            return ModifyRequestMsg.error(msg);
        }
    }


    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("payment", "[Staff] Modify a payment request");
        CommandOptionList.of(
            List.of(),
            List.of(CommandOption.REQUEST, CommandOption.PAYMENT_AMOUNT, CommandOption.DATE)
        ).addToCommand(command);
        return command;
    }

}
