package com.ambrosia.loans.database.system.log;

import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import io.ebean.Model;
import io.ebean.annotation.DbJson;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "log")
public class DLog extends Model {

    @Id
    protected UUID id;

    @Column
    protected String logCategory;
    @Column
    protected String logType;
    @Column(columnDefinition = "varchar")
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

    public DLog(DiscordLog discordLog) {
        this.logCategory = discordLog.getCategory();
        this.logType = discordLog.getLogType();
        this.message = discordLog.getMessage();
        this.json = discordLog.getJson();
        this.client = discordLog.getClient();
        UserActor actor = discordLog.getActor();
        this.actor = actor.getClient();
        this.actorDiscord = actor.getDiscordIdLong();
        this.actorDiscordName = actor.getName();
    }
}
