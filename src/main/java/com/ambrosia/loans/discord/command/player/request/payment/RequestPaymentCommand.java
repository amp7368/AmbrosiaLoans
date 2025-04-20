package com.ambrosia.loans.discord.command.player.request.payment;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.client.BaseClientSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.request.WarnBotBlockedObj;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.payment.ActiveRequestPayment;
import com.ambrosia.loans.discord.request.payment.ActiveRequestPaymentGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import com.ambrosia.loans.util.emerald.Emeralds;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class RequestPaymentCommand extends BaseClientSubCommand {

    @Override
    public void onClientCommand(SlashCommandInteractionEvent event, DClient client) {
        Instant timestamp = Instant.now();
        Optional<DLoan> loan = client.getActiveLoan();
        if (loan.isEmpty()) {
            replyError(event, "You have no active loans!");
            return;
        }
        Emeralds loanAmount = loan.get().getTotalOwed(timestamp);
        Emeralds payment;
        Boolean isFull = CommandOption.PAYMENT_FULL.getOptional(event);
        if (isFull != null && isFull) payment = loanAmount;
        else payment = CommandOption.PAYMENT_AMOUNT.getOptional(event);
        if (payment == null) {
            replyError(event, "Either 'full' or 'amount' must be entered to specify the payment amount");
            return;
        }
        if (payment.lte(0)) {
            ErrorMessages.amountNotPositive(payment)
                .replyError(event);
            return;
        }
        if (client.getActiveLoan().isEmpty()) {
            ErrorMessages.onlyLoans().replyError(event);
            return;
        }
        if (loanAmount.lt(payment.amount())) {
            ErrorMessages.paymentTooMuch(loanAmount, payment)
                .replyError(event);
            return;
        }

        ActiveRequestPayment request = new ActiveRequestPayment(client, loan.get().getId(), payment, timestamp);

        ActiveRequestPaymentGui finishedGui = request.create();

        WarnBotBlockedObj warning = new WarnBotBlockedObj(request.getClient(), finishedGui::updateSender);

        finishedGui.guiClient(DCFEditMessage.ofReply(event::reply), null)
            .send(warning.initialSuccess(), warning.initialFailed());
        finishedGui.send(ActiveRequestDatabase::sendRequest);
        warning.tryFirstDirectMessage();
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("payment", "Request to submit a payment");
        CommandOptionList.of(
            List.of(),
            List.of(CommandOption.PAYMENT_AMOUNT, CommandOption.PAYMENT_FULL)
        ).addToCommand(command);
        return command;
    }
}
