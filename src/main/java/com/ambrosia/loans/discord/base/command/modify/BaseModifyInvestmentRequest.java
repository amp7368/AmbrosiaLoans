package com.ambrosia.loans.discord.base.command.modify;

import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.request.investment.ActiveRequestInvestmentGui;
import com.ambrosia.loans.util.emerald.Emeralds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface BaseModifyInvestmentRequest extends BaseModifyRequest {

    default ActiveRequestInvestmentGui findInvestmentRequest(SlashCommandInteractionEvent event) {
        return findRequest(event, ActiveRequestInvestmentGui.class);
    }

    default ModifyRequestMsg setAmount(ActiveRequestInvestmentGui investment, SlashCommandInteractionEvent event) {
        Emeralds amount = CommandOption.INVESTMENT_AMOUNT.getRequired(event);
        if (amount == null) return null;

        investment.getData().setAmount(amount);
        return ModifyRequestMsg.info("%s set as investment amount".formatted(amount));
    }
}
