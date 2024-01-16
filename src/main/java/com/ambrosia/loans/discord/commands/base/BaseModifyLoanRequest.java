package com.ambrosia.loans.discord.commands.base;

import com.ambrosia.loans.database.entity.client.ClientApi;
import com.ambrosia.loans.discord.active.ActiveRequestDatabase;
import com.ambrosia.loans.discord.active.cash.ActiveRequestLoanGui;
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
        @Nullable Long loanId = findOption(event, "request_id", OptionMapping::getAsLong);
        if (loanId == null) return null;
        DCFStoredGui<?> request = ActiveRequestDatabase.get().getRequest(loanId);
        if (request == null) {
            replyError(event, "There is no request with id '%d'!".formatted(loanId));
            return null;
        }
        if (!(request instanceof ActiveRequestLoanGui loan)) {
            replyError(event, "Request #%d is not a loan request".formatted(loanId));
            return null;
        }
        return loan;
    }

    default ModifyRequestMsg setVouch(ActiveRequestLoanGui loan, SlashCommandInteractionEvent event) {
        String vouch = findOption(event, "vouch", OptionMapping::getAsString);
        if (vouch == null) return null;
        ClientApi vouchClient = ClientApi.findByName(vouch);
        if (vouchClient.isEmpty())
            return ModifyRequestMsg.error("Cannot find client '%s'".formatted(vouch));
        loan.getData().setVouchClient(vouchClient.getEntity());
        return ModifyRequestMsg.info("%s set as vouch".formatted(vouchClient.entity.getEffectiveName()));
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
