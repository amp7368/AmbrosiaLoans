package com.ambrosia.loans.discord.active.base;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class ActiveRequestSender {

    private String username;
    private String avatarUrl;
    private long discordId;
    private DClient client;

    public ActiveRequestSender(Member sender, DClient client) {
        this.username = sender.getUser().getEffectiveName();
        this.avatarUrl = sender.getEffectiveAvatarUrl();
        this.discordId = sender.getIdLong();
        this.client = client;
    }

    public ActiveRequestSender() {
    }

    public void author(EmbedBuilder embed) {
        String name = client.getMinecraft(ClientMinecraftDetails::getName);
        embed.setAuthor(String.format("%s - (%s)", name == null ? "NA" : name, username), null,
            avatarUrl);
    }

    public void sendDm(MessageCreateData message) {
        DiscordBot.dcf.jda()
            .openPrivateChannelById(this.discordId)
            .queue(dm -> dm.sendMessage(message).queue());
    }

    public void setClient(DClient client) {
        this.client = client;
    }
}
