package com.ambrosia.loans.database.alter.change;

import com.ambrosia.loans.database.alter.create.DAlterCreate;
import com.ambrosia.loans.database.alter.type.AlterChangeType;
import com.ambrosia.loans.database.alter.type.AlterGson;
import io.ebean.Model;
import io.ebean.annotation.DbJson;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "alter_change")
public class DAlterChange extends Model {

    @Id
    private long id;

    @ManyToOne(optional = false)
    private DAlterCreate entity;

    @Column(nullable = false)
    private AlterChangeType type;
    @Column(nullable = false)
    private Timestamp eventDate;
    @Column(nullable = false)
    private boolean applied;
    @DbJson
    @Column(nullable = false)
    private String obj;

    @OneToMany
    private List<DAlterChangeUndoHistory> history;

    private transient AlterDBChange<?, ?> objDeserialized;

    public DAlterChange(AlterDBChange<?, ?> change, DAlterCreate entity) {
        this.eventDate = Timestamp.from(change.getAppliedDate());
        this.applied = true;
        this.entity = entity;
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
        return this.entity.getEntityId();
    }

    public String getEntityType() {
        return this.entity.getEntityType();
    }

    public String getEntityDisplayName() {
        return this.entity.getEntityDisplayName();
    }

    public AlterChangeType getAlterType() {
        return this.type;
    }

    public boolean isApplied() {
        return applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    public void addHistory(DAlterChangeUndoHistory undo) {
        this.history.add(undo);
    }

    public AlterDBChange<?, ?> getObj() {
        if (this.objDeserialized != null)
            return this.objDeserialized;
        this.objDeserialized = (AlterDBChange<?, ?>) AlterGson.alterDBFromJson(this.obj);
        this.objDeserialized.init(this);
        return this.objDeserialized;
    }
}
