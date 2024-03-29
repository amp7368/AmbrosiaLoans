package com.ambrosia.loans.discord.request.payment;

import com.ambrosia.loans.discord.request.base.BaseModifyRequest;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;

public interface BaseModifyPaymentRequest extends BaseModifyRequest {

    @Nullable
    default ActiveRequestPaymentGui findPaymentRequest(SlashCommandInteractionEvent event, boolean isStaff) {
        return findRequest(event, ActiveRequestPaymentGui.class, "payment", isStaff);
    }
}
