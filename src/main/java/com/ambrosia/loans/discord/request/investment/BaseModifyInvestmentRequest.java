package com.ambrosia.loans.discord.request.investment;

import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.request.base.BaseModifyRequest;
import com.ambrosia.loans.discord.request.base.ModifyRequestMsg;
import com.ambrosia.loans.util.emerald.Emeralds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface BaseModifyInvestmentRequest extends BaseModifyRequest {

    default ActiveRequestInvestmentGui findInvestmentRequest(SlashCommandInteractionEvent event, boolean isStaff) {
        return findRequest(event, ActiveRequestInvestmentGui.class, "investment", isStaff);
    }

    default ModifyRequestMsg setAmount(ActiveRequestInvestmentGui investment, SlashCommandInteractionEvent event) {
        Emeralds amount = CommandOption.INVESTMENT_AMOUNT.getRequired(event);
        if (amount == null) return null;
        if (!amount.isPositive()) {
            return ModifyRequestMsg.error("'Initial amount' must be positive! Provided: %s.".formatted(amount));
        }
        investment.getData().setAmount(amount);
        return ModifyRequestMsg.info("%s set as investment amount".formatted(amount));
    }
}
