package com.ambrosia.loans.database.alter.change;

import com.ambrosia.loans.database.alter.UndoHistory;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "alter_change_undo_history")
public class DAlterChangeUndoHistory extends UndoHistory {

    @ManyToOne(optional = false)
    private final DAlterChange alterRecord;

    public DAlterChangeUndoHistory(DStaffConductor conductor, DAlterChange alterRecord, boolean applied) {
        super(conductor, applied);
        this.alterRecord = alterRecord;
    }

    public DAlterChange getRecord() {
        return alterRecord;
    }
}
