package com.ambrosia.loans.discord.commands.player.request.invest;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.modify.BaseModifyInvestmentRequest;
import com.ambrosia.loans.discord.base.command.modify.ModifyRequestMsg;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.request.investment.ActiveRequestInvestmentGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class RequestModifyInvestmentCommand extends BaseSubCommand implements BaseModifyInvestmentRequest {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        ActiveRequestInvestmentGui investment = findInvestmentRequest(event, false);
        if (investment == null) return;
        DClient client = investment.getData().getClient();
        boolean isUser = client.isUser(event.getUser());
        if (!isUser) {
            ErrorMessages.notClient(client).replyError(event);
            return;
        }
        List<ModifyRequestMsg> changes = new ArrayList<>();
        changes.add(setAmount(investment, event));
        replyChanges(event, changes, investment);
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("investment", "Modify an investment request");
        CommandOptionList.of(
            List.of(CommandOption.REQUEST),
            List.of(CommandOption.INVESTMENT_AMOUNT)
        ).addToCommand(command);
        return command;
    }
}
