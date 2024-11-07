package com.ambrosia.loans.database.message;

import com.ambrosia.loans.util.IBaseMessageId;
import io.ebean.Model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import net.dv8tion.jda.api.entities.Message;

@Entity
@Table(name = "message_id")
public class DMessageId extends Model implements IBaseMessageId {

    @ManyToOne
    private DClientMessage clientMessage;

    @Id
    private long messageId;
    @Column
    private long channelId;
    @Column
    private long serverId;

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
}
