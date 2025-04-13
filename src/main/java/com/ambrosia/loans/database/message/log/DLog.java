package com.ambrosia.loans.database.message.log;

import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.system.log.SendDiscordLog;
import com.ambrosia.loans.util.exception.StackTraceUtils;
import io.ebean.Model;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.DbJson;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import net.dv8tion.jda.api.entities.Message;

@Entity
@Table(name = "log")
public class DLog extends Model {

    @Id
    protected UUID id;
    @DbDefault("1970-01-01 00:00:00+00")
    @Column(nullable = false)
    protected Timestamp createdAt;
    @Column
    protected String logCategory;
    @Column
    protected String logType;
    @Column(columnDefinition = "text")
    protected String message;
    @DbJson
    protected Map<String, Object> json;
    @ManyToOne
    protected DClient client;
    @ManyToOne
    protected DClient actor;
    @Column
    protected Long actorDiscord;
    @Column
    protected String actorDiscordName;
    @Column(columnDefinition = "text")
    protected String exception;
    @DbDefault("0")
    @Column(nullable = false)
    protected long discordMessageId;
    @DbDefault("0")
    @Column(nullable = false)
    protected long discordChannelId;
    @DbDefault("0")
    @Column(nullable = false)
    protected long discordServerId;

    public DLog(SendDiscordLog discordLog) {
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.logCategory = discordLog.getCategory();
        this.logType = discordLog.getLogType();
        this.message = discordLog.getMessage();
        this.json = discordLog.getJson();
        this.client = discordLog.getClient();
        UserActor actor = discordLog.getActor();
        this.actor = actor.getClient();
        this.actorDiscord = actor.getDiscordIdLong();
        this.actorDiscordName = actor.getName();
        this.exception = StackTraceUtils.stacktraceToString(discordLog.getException());
    }

    public UUID getId() {
        return id;
    }

    public void setDiscordMessage(Message discordMessage) {
        this.discordMessageId = discordMessage.getIdLong();
        this.discordChannelId = discordMessage.getChannelIdLong();
        this.discordServerId = discordMessage.getGuildIdLong();
        this.save();
    }
}
