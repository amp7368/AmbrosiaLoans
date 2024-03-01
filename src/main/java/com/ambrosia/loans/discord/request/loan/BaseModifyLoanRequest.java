package com.ambrosia.loans.discord.request.loan;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.request.base.BaseModifyRequest;
import com.ambrosia.loans.discord.request.base.ModifyRequestMsg;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;

public interface BaseModifyLoanRequest extends BaseModifyRequest {

    @Nullable
    default ActiveRequestLoanGui findLoanRequest(SlashCommandInteractionEvent event, boolean isStaff) {
        return findRequest(event, ActiveRequestLoanGui.class, "loan", isStaff);
    }

    default ModifyRequestMsg setVouch(ActiveRequestLoanGui loan, SlashCommandInteractionEvent event) {
        String vouch = CommandOption.LOAN_VOUCH.getMap1(event);
        if (vouch == null) return null;
        DClient vouchClient = CommandOption.LOAN_VOUCH.getOptional(event);
        if (vouchClient == null)
            return ModifyRequestMsg.error("Cannot find client '%s'".formatted(vouch));
        loan.getData().setVouchClient(vouchClient.getEntity());
        return ModifyRequestMsg.info("%s set as vouch".formatted(vouchClient.getEffectiveName()));
    }
}
