package com.ambrosia.loans.database.config.model;

import com.ambrosia.loans.database.config.AppConfigMeta;
import io.ebean.Model;
import io.ebean.annotation.History;
import io.ebean.annotation.Identity;
import io.ebean.annotation.WhenCreated;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@History
@Entity
@Table(name = "config_type")
public class DConfigType extends Model {

    @Id
    @Identity(start = 1000)
    protected long id;
    @WhenCreated
    @Column(nullable = false)
    protected Instant createdAt;
    @Column(nullable = false, unique = true)
    protected String name;
    @Column(nullable = false)
    protected String description;

    public DConfigType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasMetaChanged(AppConfigMeta<?> meta) {
        return !description.equals(meta.configTypeDescription());
    }

    public DConfigType updateMeta(AppConfigMeta<?> meta) {
        this.description = meta.configTypeDescription();
        return this;
    }
}
