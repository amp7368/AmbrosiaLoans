package com.ambrosia.loans.discord.command.player.request.invest;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.base.command.client.BaseClientSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.message.tos.AcceptTOSGui;
import com.ambrosia.loans.discord.message.tos.AcceptTOSRequest;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.investment.ActiveRequestInvestment;
import com.ambrosia.loans.discord.request.investment.ActiveRequestInvestmentGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class RequestInvestmentCommand extends BaseClientSubCommand {

    private boolean checkErrors(SlashCommandInteractionEvent event, DClient client, Emeralds amount) {
        if (amount == null) return true;
        Optional<DLoan> activeLoan = client.getActiveLoan();
        if (activeLoan.isPresent()) {
            ErrorMessages.hasActiveLoan(activeLoan.get()).replyError(event);
            return true;
        }
        Emeralds balance = client.getBalance(Instant.now());
        if (balance.isNegative()) {
            ErrorMessages.onlyInvestments(balance).replyError(event);
            return true;
        }
        if (amount.lte(0)) {
            ErrorMessages.amountNotPositive(amount).replyError(event);
            return true;
        }
        return false;
    }

    public void onAccept(ButtonInteractionEvent event, ActiveRequestInvestment request) {
        ActiveRequestInvestmentGui finishedGui = request.create();
        event.reply(finishedGui.makeClientMessage()).queue();
        finishedGui.send(ActiveRequestDatabase::sendRequest);
        finishedGui.updateSender();
    }

    private void onReject(ButtonInteractionEvent event, ActiveRequestInvestment request) {
        MessageCreateData msg = ErrorMessages.rejectedTOSRequest("investment").createMsg();
        event.reply(msg).setEphemeral(true).queue();
        request.saveArchive();
    }

    @Override
    public void onClientCommand(SlashCommandInteractionEvent event, DClient client) {
        Emeralds amount = CommandOption.INVESTMENT_AMOUNT.getRequired(event);
        if (checkErrors(event, client, amount)) return;

        ActiveRequestInvestment request = new ActiveRequestInvestment(client, amount);

        AcceptTOSGui gui = new AcceptTOSGui(DiscordBot.dcf, event::reply);
        new AcceptTOSRequest(gui, client,
            e -> onAccept(e, request),
            e -> onReject(e, request)
        ).send();
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("investment", "Request to make an investment");
        CommandOptionList.of(
            List.of(CommandOption.INVESTMENT_AMOUNT)
        ).addToCommand(command);
        return command;
    }
}
