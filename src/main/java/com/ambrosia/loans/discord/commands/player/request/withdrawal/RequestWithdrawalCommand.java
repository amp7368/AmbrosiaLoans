package com.ambrosia.loans.discord.commands.player.request.withdrawal;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.client.BaseClientSubCommand;
import com.ambrosia.loans.discord.base.command.modify.BaseModifyWithdrawalRequest;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.withdrawal.ActiveRequestWithdrawal;
import com.ambrosia.loans.discord.request.withdrawal.ActiveRequestWithdrawalGui;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class RequestWithdrawalCommand extends BaseClientSubCommand implements BaseModifyWithdrawalRequest {

    @Override
    public void onClientCommand(SlashCommandInteractionEvent event, DClient client) {
        Emeralds amount;
        Boolean isFull = CommandOption.WITHDRAWAL_FULL.getOptional(event);
        if (isFull != null && isFull) amount = client.getBalance(Instant.now());
        else amount = CommandOption.WITHDRAWAL_AMOUNT.getOptional(event);
        if (amount == null) {
            replyError(event, "Either 'full' or 'amount' must be entered to specify the payment amount");
            return;
        }
        if (checkErrors(event, client, amount)) return;

        ActiveRequestWithdrawal request = new ActiveRequestWithdrawal(client, amount.negative());

        ActiveRequestWithdrawalGui gui = request.create();
        event.reply(gui.makeClientMessage()).queue();
        gui.send(ActiveRequestDatabase::sendRequest);

    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("withdrawal", "Request to make a withdrawal");
        CommandOptionList.of(
            List.of(),
            List.of(CommandOption.WITHDRAWAL_AMOUNT, CommandOption.WITHDRAWAL_FULL)
        ).addToCommand(command);
        return command;
    }
}
