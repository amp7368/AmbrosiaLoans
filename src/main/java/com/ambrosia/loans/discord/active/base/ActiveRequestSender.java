package com.ambrosia.loans.discord.active.base;

import com.ambrosia.loans.database.entity.client.ClientApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.discord.DiscordBot;
import java.util.Optional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class ActiveRequestSender {

    private long clientId;
    private transient DClient client;

    public ActiveRequestSender(DClient client) {
        this.clientId = client.getId();
        this.client = client;
    }

    public ActiveRequestSender() {
    }

    public DClient getClient() {
        if (client != null) return client;
        return client = ClientApi.findById(this.clientId).entity;
    }

    public void setClient(DClient client) {
        this.client = client;
    }

    public void author(EmbedBuilder embed) {
        DClient client = getClient();
        Optional<String> name = Optional.ofNullable(client.getMinecraft(ClientMinecraftDetails::getName));

        String url = name.isPresent() ? "https://wynncraft.com/stats/player/" + name : null;
        String discordName = client.getDiscord(ClientDiscordDetails::getUsername);
        String author = String.format("%s - (%s)", name.orElse("NA"), discordName);
        embed.setAuthor(author, url, client.getDiscord().getAvatarUrl());
    }

    public void sendDm(MessageCreateData message) {
        DiscordBot.dcf.jda()
            .openPrivateChannelById(this.getClient().getDiscord(ClientDiscordDetails::getDiscordId))
            .queue(dm -> dm.sendMessage(message).queue());
    }
}
