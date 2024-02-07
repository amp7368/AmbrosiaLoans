package com.ambrosia.loans.discord.command.staff.modify;

import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.request.investment.BaseModifyInvestmentRequest;
import com.ambrosia.loans.discord.request.base.ModifyRequestMsg;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.request.investment.ActiveRequestInvestmentGui;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class AModifyInvestmentCommand extends BaseSubCommand implements BaseModifyInvestmentRequest {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        ActiveRequestInvestmentGui loan = findInvestmentRequest(event, true);
        if (loan == null) return;
        List<ModifyRequestMsg> changes = new ArrayList<>();
        changes.add(setAmount(loan, event));
        replyChanges(event, changes, loan);
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("investment", "[Staff] Modify an investment request");
        CommandOptionList.of(
            List.of(CommandOption.REQUEST),
            List.of(CommandOption.INVESTMENT_AMOUNT)
        ).addToCommand(command);
        return command;
    }
}
