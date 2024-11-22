package com.ambrosia.loans.database.message;

import com.ambrosia.loans.discord.DiscordBot;
import discord.util.dcf.util.message.DiscordMessageId;
import discord.util.dcf.util.message.IDiscordMessageId;
import io.ebean.Model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import net.dv8tion.jda.api.entities.Message;

@Entity
@Table(name = "message_id")
public class DMessageId extends Model implements IDiscordMessageId {

    @ManyToOne
    private DClientMessage clientMessage;

    @Id
    private long messageId;
    @Column
    private long channelId;
    @Column
    private long serverId;

    private transient DiscordMessageId data;

    public DMessageId(Message message) {
        this.messageId = message.getIdLong();
        this.channelId = message.getChannelIdLong();
        this.serverId = message.getGuildIdLong();
    }

    @Override
    public long getChannelId() {
        return channelId;
    }

    @Override
    public long getServerId() {
        return serverId;
    }

    @Override
    public long getMessageId() {
        return messageId;
    }

    public DMessageId setClient(DClientMessage client) {
        this.clientMessage = client;
        return this;
    }

    public DiscordMessageId getMessage() {
        if (this.data != null) return data;
        return this.data = IDiscordMessageId.withDCF(this, DiscordBot.dcf);
    }
}
