package com.ambrosia.loans.discord.base.command.modify;

import com.ambrosia.loans.discord.request.payment.ActiveRequestPaymentGui;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;

public interface BaseModifyPaymentRequest extends BaseModifyRequest {

    @Nullable
    default ActiveRequestPaymentGui findPaymentRequest(SlashCommandInteractionEvent event) {
        return findRequest(event, ActiveRequestPaymentGui.class);
    }
}
