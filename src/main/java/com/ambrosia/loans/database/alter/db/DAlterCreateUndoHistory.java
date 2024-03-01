package com.ambrosia.loans.database.alter.db;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "alter_create_undo_history")
public class DAlterCreateUndoHistory extends UndoHistory {

    @ManyToOne(optional = false)
    private final DAlterCreate alterCreate;

    public DAlterCreateUndoHistory(DStaffConductor conductor, DAlterCreate alterCreate, boolean applied) {
        super(conductor, applied);
        this.alterCreate = alterCreate;
    }

    public DAlterCreate getRecord() {
        return alterCreate;
    }

}
