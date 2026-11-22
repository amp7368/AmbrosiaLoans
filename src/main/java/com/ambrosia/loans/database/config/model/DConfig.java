package com.ambrosia.loans.database.config.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.Model;
import io.ebean.annotation.DbJson;
import io.ebean.annotation.History;
import io.ebean.annotation.Identity;
import io.ebean.annotation.WhenCreated;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.jetbrains.annotations.NotNull;

@History
@Entity
@Table(name = "config")
@UniqueConstraint(columnNames = {"config_type_id", "date_effective"})
public class DConfig extends Model {

    @Id
    @Identity(start = 10000)
    protected long id;
    @ManyToOne
    protected DConfigType configType;
    @WhenCreated
    @Column(nullable = false)
    protected Instant dateEffective;

    @Column(nullable = false)
    protected ConfigUpdateType updateType;
    @Column
    protected String updateMsg;
    @Column
    protected String discordMsgReference;

    @DbJson
    protected JsonNode config;

    public DConfig(@NotNull DConfigType configType, @NotNull ConfigUpdateType updateType,
        String updateMsg, String discordMsgReference, JsonNode config) {
        this.configType = configType;
        this.updateType = updateType;
        this.updateMsg = updateMsg;
        this.discordMsgReference = discordMsgReference;
        this.config = config;
    }

    public Instant getDateEffective() {
        return dateEffective;
    }

    public long getId() {
        return id;
    }

    public DConfigType getConfigType() {
        return configType;
    }

    public ConfigUpdateType getUpdateType() {
        return updateType;
    }

    public String getUpdateMsg() {
        return updateMsg;
    }

    public String getDiscordMsgReference() {
        return discordMsgReference;
    }

    public JsonNode getJson() {
        return config;
    }

}
