package com.ambrosia.loans.database.message;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.service.message.base.SentClientMessage;
import com.ambrosia.loans.service.message.base.SentClientMessageType;
import io.ebean.Model;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.DbJson;
import io.ebean.config.dbplatform.DbDefaultValue;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import net.dv8tion.jda.api.entities.Message;

@Entity
@Table(name = "message_client")
public class DClientMessage extends Model {

    @Id
    protected UUID id;
    @ManyToOne
    protected DClient client;
    @Column
    protected MessageReason reason;
    @Column
    protected String message;
    @Column
    protected long messageId;
    @OneToMany
    protected List<DMessageId> staffMessages = new ArrayList<>();
    @DbDefault(DbDefaultValue.NOW)
    @Column(nullable = false)
    protected Timestamp dateCreated;
    @Column
    protected Timestamp dateAcknowledged;
    @Column(nullable = false)
    protected MessageAcknowledged acknowledged;
    @DbJson
    protected String sentMessageObj;

    public DClientMessage(DClient client, MessageReason reason, String message,
        Instant dateCreated, SentClientMessage sentClientMessage) {
        this.client = client;
        this.reason = reason;
        this.message = message;
        this.dateCreated = Timestamp.from(dateCreated);
        this.acknowledged = MessageAcknowledged.SENDING;
        setSentMessage(sentClientMessage);
    }

    public Instant getDateCreated() {
        return dateCreated.toInstant();
    }

    public UUID getId() {
        return this.id;
    }

    public long getMessageId() {
        return this.messageId;
    }

    public SentClientMessage getSentMessage() {
        return SentClientMessageType.gson()
            .fromJson(this.sentMessageObj, SentClientMessage.class)
            .load(this);
    }

    public DClientMessage setSentMessage(SentClientMessage sentMessage) {
        this.sentMessageObj = SentClientMessageType.gson().toJson(sentMessage);
        return this;
    }

    public DClientMessage acknowledge() {
        this.acknowledged = MessageAcknowledged.ACKNOWLEDGED;
        return this;
    }

    public MessageAcknowledged getStatus() {
        return acknowledged;
    }

    public DClient getClient() {
        return this.client;
    }

    public List<DMessageId> getStaffMessages() {
        return this.staffMessages;
    }

    public DClientMessage setMessage(Message msg) {
        this.acknowledged = MessageAcknowledged.SENDING;
        this.messageId = msg.getIdLong();
        return this;
    }
}
