package com.ambrosia.loans.database.alter.db;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import io.ebean.Model;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "alter_change_undo_history")
public class DAlterUndoHistory extends Model {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    private final DAlterChangeRecord alterRecord;

    @Column(nullable = false)
    private final Timestamp completedAt = Timestamp.from(Instant.now());
    @Column(nullable = false)
    private final boolean applied;
    @ManyToOne(optional = false)
    private final DStaffConductor conductor;

    public DAlterUndoHistory(DStaffConductor conductor, DAlterChangeRecord alterRecord, boolean applied) {
        this.alterRecord = alterRecord;
        this.applied = applied;
        this.conductor = conductor;
    }

    public DAlterChangeRecord getRecord() {
        return alterRecord;
    }

    public Instant getCompletedAt() {
        return completedAt.toInstant();
    }

}
