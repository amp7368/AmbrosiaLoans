package com.ambrosia.loans.discord.commands.player.request.invest;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.client.BaseClientSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.investment.ActiveRequestInvestment;
import com.ambrosia.loans.discord.request.investment.ActiveRequestInvestmentGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

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

    @Override
    public void onClientCommand(SlashCommandInteractionEvent event, DClient client) {
        Emeralds amount = CommandOption.INVESTMENT_AMOUNT.getRequired(event);
        if (checkErrors(event, client, amount)) return;

        ActiveRequestInvestment request = new ActiveRequestInvestment(client, amount);

        ActiveRequestInvestmentGui gui = request.create();
        event.reply(gui.makeClientMessage()).queue();
        gui.send(ActiveRequestDatabase::sendRequest);

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
