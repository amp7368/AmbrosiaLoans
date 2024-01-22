package com.ambrosia.loans.discord.base.command.modify;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoanGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import discord.util.dcf.gui.stored.DCFStoredGui;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;

public interface BaseModifyLoanRequest extends BaseModifyRequest {

    @Nullable
    default ActiveRequestLoanGui findLoanRequest(SlashCommandInteractionEvent event) {
        Long requestId = CommandOption.REQUEST.getMap1(event);
        DCFStoredGui<?> request = CommandOption.REQUEST.getRequired(event, ErrorMessages.noRequestWithId(requestId));
        if (request instanceof ActiveRequestLoanGui loan)
            return loan;
        if (request != null)
            ErrorMessages.badRequestType("loan", requestId).replyError(event);
        return null;
    }

    default ModifyRequestMsg setVouch(ActiveRequestLoanGui loan, SlashCommandInteractionEvent event) {
        String vouch = CommandOption.VOUCH.getMap1(event);
        if (vouch == null) return null;
        DClient vouchClient = CommandOption.VOUCH.getOptional(event);
        if (vouchClient == null)
            return ModifyRequestMsg.error("Cannot find client '%s'".formatted(vouch));
        loan.getData().setVouchClient(vouchClient.getEntity());
        return ModifyRequestMsg.info("%s set as vouch".formatted(vouchClient.getEffectiveName()));
    }

    default void replyChanges(SlashCommandInteractionEvent event, List<ModifyRequestMsg> changes, ActiveRequestLoanGui loan) {
        changes.removeIf(Objects::isNull);
        if (changes.isEmpty()) {
            replyError(event, "No changes were specified");
            return;
        }
        String description = changes.stream()
            .map(ModifyRequestMsg::toString)
            .collect(Collectors.joining("\n"));
        replySuccess(event, description);
        loan.editMessage();
    }
}
