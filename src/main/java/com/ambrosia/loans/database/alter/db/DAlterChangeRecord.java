package com.ambrosia.loans.database.alter.db;

import com.ambrosia.loans.database.alter.base.AlterDBChange;
import com.ambrosia.loans.database.alter.gson.AlterGson;
import com.ambrosia.loans.database.alter.gson.AlterRecordType;
import io.ebean.Model;
import io.ebean.annotation.DbJson;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "alter_change_record")
public class DAlterChangeRecord extends Model {

    @Id
    private long id;

    @Column(nullable = false)
    private long entityId;
    @Column(nullable = false)
    private AlterRecordType type;

    @Column(nullable = false)
    private Timestamp eventDate;
    @Column(nullable = false)
    private boolean applied;
    @DbJson
    @Column(nullable = false)
    private String obj;

    @OneToMany
    private List<DAlterUndoHistory> history;
    private transient AlterDBChange<?, ?> objDeserialized;

    public DAlterChangeRecord(AlterDBChange<?, ?> change) {
        this.eventDate = Timestamp.from(change.getAppliedDate());
        this.applied = true;
        this.entityId = change.getEntityId();
        this.type = change.getType();
        this.obj = change.toJson();
    }

    public long getId() {
        return this.id;
    }

    public Instant getEventDate() {
        return this.eventDate.toInstant();
    }

    public long getEntityId() {
        return this.entityId;
    }

    public AlterRecordType getAlterType() {
        return this.type;
    }

    public boolean isApplied() {
        return applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    public void addHistory(DAlterUndoHistory undo) {
        this.history.add(undo);
    }

    public AlterDBChange<?, ?> getChangeObj() {
        if (this.objDeserialized != null) return this.objDeserialized;
        this.objDeserialized = (AlterDBChange<?, ?>) AlterGson.alterDBFromJson(this.obj);
        this.objDeserialized.init(this);
        return this.objDeserialized;
    }

    public String getEntityName() {
        return this.getChangeObj().getEntityTypeName();
    }
}
