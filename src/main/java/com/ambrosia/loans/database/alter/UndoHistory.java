package com.ambrosia.loans.database.alter;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import io.ebean.Model;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class UndoHistory extends Model {

    @Id
    private UUID id;

    @Column(nullable = false)
    private final Timestamp completedAt = Timestamp.from(Instant.now());
    @Column(nullable = false)
    private final boolean applied;
    @ManyToOne(optional = false)
    private final DStaffConductor conductor;

    public UndoHistory(DStaffConductor conductor, boolean applied) {
        this.applied = applied;
        this.conductor = conductor;
    }

    public Instant getCompletedAt() {
        return completedAt.toInstant();
    }
}
