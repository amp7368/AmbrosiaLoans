package com.ambrosia.loans.service.message.dms;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.username.ClientDiscordDetails;
import com.ambrosia.loans.database.message.MessageReason;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import com.ambrosia.loans.service.message.base.SentClientMessage;
import com.ambrosia.loans.service.message.base.SentClientMessageType;
import java.time.Instant;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

public class ManualClientMessage extends SentClientMessage {

    private transient DClient sender;
    private long senderId;
    private String title;

    public ManualClientMessage() {
        super(SentClientMessageType.MANUAL);
    }

    public ManualClientMessage(DClient sender, DClient client, String title, String message) {
        super(SentClientMessageType.MANUAL, client);
        this.sender = sender;
        this.senderId = sender.getId();
        this.title = titleOrDefault(title);
        this.setDescription(message);
    }

    public String titleOrDefault(String title) {
        if (title != null) return title;
        String senderUsername = getSender().getDiscord(ClientDiscordDetails::getUsername);
        return "@%s sent you a message!".formatted(senderUsername);
    }

    @Override
    public MessageReason getReason() {
        return MessageReason.MANUAL;
    }

    public DClient getSender() {
        if (sender != null)
            return sender;
        return sender = ClientQueryApi.findById(senderId);
    }

    @Override
    protected MessageCreateData makeClientMessage() {
        EmbedBuilder embed = makeBaseEmbed();
        ClientMessage.of(getSender()).clientAuthor(embed);
        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .build();
    }

    @Override
    protected MessageCreateData makeStaffMessage() {
        EmbedBuilder embed = makeBaseEmbed();
        ClientMessage.of(getClient()).clientAuthor(embed);
        embed.setTitle("Sent message")
            .setDescription("### %s\n".formatted(title))
            .appendDescription(quoteText(getDescription()));
        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .build();
    }

    @Override
    protected EmbedBuilder makeBaseEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(AmbrosiaColor.BLUE_NORMAL);
        embed.setTitle(title);
        @Nullable ClientDiscordDetails discord = getSender().getDiscord();
        if (discord != null)
            embed.setFooter("Sent by " + discord.getUsername(), discord.getAvatarUrl());
        embed.setTimestamp(Instant.now());
        embed.appendDescription(getDescription());
        return embed;
    }

    @Override
    protected boolean canInteract() {
        return false;
    }
}
