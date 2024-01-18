package com.ambrosia.loans.discord.base.command.modify;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.cash.ActiveRequestLoanGui;
import discord.util.dcf.gui.stored.DCFStoredGui;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.Nullable;

public interface BaseModifyLoanRequest extends BaseModifyRequest {

    @Nullable
    default ActiveRequestLoanGui findLoanRequest(SlashCommandInteractionEvent event) {
        @Nullable Long requestId = findOption(event, "request_id", OptionMapping::getAsLong);
        if (requestId == null) return null;
        DCFStoredGui<?> request = ActiveRequestDatabase.get().getRequest(requestId);
        if (request == null) {
            replyError(event, "There is no request with id '%d'!".formatted(requestId));
            return null;
        }
        if (!(request instanceof ActiveRequestLoanGui loan)) {
            replyError(event, "Request #%d is not a loan request".formatted(requestId));
            return null;
        }
        return loan;
    }

    default ModifyRequestMsg setVouch(ActiveRequestLoanGui loan, SlashCommandInteractionEvent event) {
        String vouch = CommandOption.VOUCH.getOptionalMap1(event);
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

    default OptionData optionVouch() {
        String description = "Referral/vouch from someone with credit with Ambrosia";
        return new OptionData(OptionType.STRING, "vouch", description, false, true);
    }

    default OptionData optionDiscount() {
        String description = "Vouchers & Referral Codes";
        return new OptionData(OptionType.STRING, "discount", description, false, false);
    }
}
