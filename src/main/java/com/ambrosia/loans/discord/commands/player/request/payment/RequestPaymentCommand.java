package com.ambrosia.loans.discord.commands.player.request.payment;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.base.command.client.BaseClientSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.payment.ActiveRequestPayment;
import com.ambrosia.loans.discord.request.payment.ActiveRequestPaymentGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.Optional;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class RequestPaymentCommand extends BaseClientSubCommand {

    @Override
    public void onClientCommand(SlashCommandInteractionEvent event, DClient client) {
        Instant timestamp = Instant.now();
        Emeralds loanAmount = client.getBalance(timestamp).negative();
        Emeralds payment = CommandOption.PAYMENT_AMOUNT.getRequired(event);
        if (payment == null) return;
        if (payment.lte(0)) {
            ErrorMessages.amountNotPositive(payment)
                .replyError(event);
            return;
        }
        if (loanAmount.lte(0)) {
            ErrorMessages.onlyLoans(loanAmount.negative())
                .replyError(event);
            return;
        }
        if (loanAmount.lt(payment.amount())) {
            ErrorMessages.paymentTooMuch(loanAmount, payment)
                .replyError(event);
            return;
        }
        Optional<DLoan> loan = client.getActiveLoan();
        if (loan.isEmpty()) {
            replyError(event, "You have no active loans!");
            String error = "Client %s has a balance of %s, and wants to make a payment of %s, but has no active loans!"
                .formatted(client.getEffectiveName(), loanAmount, payment);
            DiscordModule.get().logger().error(error);
            return;
        }

        ActiveRequestPayment request = new ActiveRequestPayment(client, payment, timestamp);

        ActiveRequestPaymentGui gui = request.create();
        event.reply(gui.makeClientMessage()).queue();
        gui.send(ActiveRequestDatabase::sendRequest);

    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("payment", "Request to submit a payment");
        CommandOption.PAYMENT_AMOUNT.addOption(command, true);
        return command;
    }
}