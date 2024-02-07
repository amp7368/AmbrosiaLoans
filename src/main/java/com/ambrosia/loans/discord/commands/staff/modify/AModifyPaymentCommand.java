package com.ambrosia.loans.discord.commands.staff.modify;

import static com.ambrosia.loans.discord.DiscordModule.SIMPLE_DATE_FORMATTER;

import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.modify.BaseModifyPaymentRequest;
import com.ambrosia.loans.discord.base.command.modify.ModifyRequestMsg;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.request.payment.ActiveRequestPaymentGui;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.Nullable;

public class AModifyPaymentCommand extends BaseSubCommand implements BaseModifyPaymentRequest {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        @Nullable ActiveRequestPaymentGui request = findPaymentRequest(event, true);
        if (request == null) return;
        List<ModifyRequestMsg> changes = new ArrayList<>();
        changes.add(setPaymentAmount(request, event));
        changes.add(setDate(request, event));
        replyChanges(event, changes, request);
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
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
            String dateString = SIMPLE_DATE_FORMATTER.format(date);
            String msg = "Set the date to %s".formatted(dateString);
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
            List.of(CommandOption.REQUEST),
            List.of(CommandOption.PAYMENT_AMOUNT, CommandOption.DATE)
        ).addToCommand(command);
        return command;
    }

}
