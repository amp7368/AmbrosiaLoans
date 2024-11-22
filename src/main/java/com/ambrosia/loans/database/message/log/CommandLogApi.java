package com.ambrosia.loans.database.message.log;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;

public class CommandLogApi {

    public static void log(SlashCommandInteractionEvent event) {
        User discord = event.getUser();
        @Nullable DClient client = ClientQueryApi.findByDiscord(discord.getIdLong());
        MessageChannelUnion channel = event.getChannel();
        @Nullable Guild server = event.getGuild();

        // jda adds spaces like 'name: value' rather than 'name:value
        String options = event.getOptions().stream()
            .map(op -> {
                    String name = op.getName();
                    String value = op.getAsString();
                    return "%s:%s".formatted(name, value);
                }
            ).collect(Collectors.joining(" "));
        String command = "/%s %s".formatted(event.getFullCommandName(), options);

        DCommandLog log = new DCommandLog(command, discord, client, server, channel);
        log.save();
    }
}
