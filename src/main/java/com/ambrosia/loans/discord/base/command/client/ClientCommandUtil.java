package com.ambrosia.loans.discord.base.command.client;

import com.ambrosia.loans.database.entity.client.ClientApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

interface ClientCommandUtil extends SendMessage {

    default void getClientAndDoCommand(SlashCommandInteractionEvent event) {
        DClient client = ClientApi.findByDiscord(event.getUser().getIdLong()).entity;
        if (client == null) {
            ErrorMessages.registerWithStaff().replyError(event);
            return;
        }
        this.onClientCommand(event, client);
    }

    void onClientCommand(SlashCommandInteractionEvent event, DClient client);
}
