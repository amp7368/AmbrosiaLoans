package com.ambrosia.loans.discord.base.request;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.base.command.SendMessageClient;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class ActiveRequestSender implements SendMessageClient {

    private long clientId;
    private transient DClient client;

    public ActiveRequestSender(DClient client) {
        if (client == null) {
            this.clientId = -1;
            return;
        }
        this.clientId = client.getId();
        this.client = client;
    }

    public ActiveRequestSender() {
    }

    @Override
    public DClient getClient() {
        if (client != null) return client;
        return client = ClientQueryApi.findById(this.clientId);
    }

    public void setClient(DClient client) {
        this.client = client;
    }

    public void sendDm(MessageCreateData message) {
        DiscordBot.dcf.jda()
            .openPrivateChannelById(this.getClient().getDiscord(ClientDiscordDetails::getDiscordId))
            .queue(dm -> dm.sendMessage(message).queue());
    }
}
