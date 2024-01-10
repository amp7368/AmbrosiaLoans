package com.ambrosia.loans.discord.base.command;

import com.ambrosia.loans.database.entity.client.ClientApi;
import com.ambrosia.loans.database.entity.client.DClient;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CommandOptionClient {

    public static DClient findClient(SlashCommandInteractionEvent event) {
        return findClientApi(event).entity;
    }

    public static ClientApi findClientApi(SlashCommandInteractionEvent event) {
        return ClientApi.findByName(CommandOption.CLIENT.getRequired(event));
    }
}
