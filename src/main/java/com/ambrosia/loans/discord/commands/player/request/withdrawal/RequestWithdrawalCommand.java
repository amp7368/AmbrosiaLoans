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
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class RequestWithdrawalCommand extends BaseClientSubCommand implements BaseModifyWithdrawalRequest {

    @Override
    public void onClientCommand(SlashCommandInteractionEvent event, DClient client) {
        Emeralds amount = CommandOption.INVESTMENT_AMOUNT.getRequired(event);
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
            List.of(CommandOption.WITHDRAWAL_AMOUNT)
        ).addToCommand(command);
        return command;
    }
}
