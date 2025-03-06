package com.ambrosia.loans.discord.command.player.request.withdrawal;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.client.BaseClientSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.request.WarnBotBlockedObj;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.withdrawal.ActiveRequestWithdrawal;
import com.ambrosia.loans.discord.request.withdrawal.ActiveRequestWithdrawalGui;
import com.ambrosia.loans.discord.request.withdrawal.BaseModifyWithdrawalRequest;
import com.ambrosia.loans.util.emerald.Emeralds;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class RequestWithdrawalCommand extends BaseClientSubCommand implements BaseModifyWithdrawalRequest {

    @Override
    public void onClientCommand(SlashCommandInteractionEvent event, DClient client) {
        Emeralds amount;
        Boolean isFull = CommandOption.WITHDRAWAL_FULL.getOptional(event);
        if (isFull != null && isFull) amount = client.getInvestBalanceNow();
        else amount = CommandOption.WITHDRAWAL_AMOUNT.getOptional(event);
        if (amount == null) {
            replyError(event, "Either 'full' or 'amount' must be entered to specify the withdrawal amount");
            return;
        }
        if (checkErrors(event, client, amount)) return;

        ActiveRequestWithdrawal request = new ActiveRequestWithdrawal(client, amount.negative());

        ActiveRequestWithdrawalGui finishedGui = request.create();

        WarnBotBlockedObj warning = new WarnBotBlockedObj(request.getClient(), finishedGui::updateSender);

        finishedGui.guiClient(DCFEditMessage.ofReply(event::reply), null)
            .send(warning.initialSuccess(), warning.initialFailed());
        finishedGui.send(ActiveRequestDatabase::sendRequest);
        warning.tryFirstDirectMessage();
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
