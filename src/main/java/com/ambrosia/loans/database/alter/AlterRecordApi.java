package com.ambrosia.loans.database.alter;

import com.ambrosia.loans.database.alter.base.AlterDBChange;
import com.ambrosia.loans.database.alter.base.AlterDBCreate;
import com.ambrosia.loans.database.alter.db.DAlterChangeRecord;
import com.ambrosia.loans.database.alter.db.DAlterUndoHistory;
import com.ambrosia.loans.database.alter.db.query.QDAlterChangeRecord;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import io.ebean.DB;
import io.ebean.Transaction;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public interface AlterRecordApi {

    @Nullable
    static DAlterUndoHistory undo(DStaffConductor conductor, DAlterChangeRecord record) {
        return apply(conductor, record, false);
    }

    @Nullable
    static DAlterUndoHistory redo(DStaffConductor conductor, DAlterChangeRecord record) {
        return apply(conductor, record, true);
    }

    @Nullable
    private static DAlterUndoHistory apply(DStaffConductor conductor, DAlterChangeRecord record, boolean applied) {
        try (Transaction transaction = DB.beginTransaction()) {
            DAlterUndoHistory history = apply(conductor, record, applied, transaction);
            transaction.commit();
            record.refresh();
            return history;
        }
    }

    @Nullable
    private static DAlterUndoHistory apply(DStaffConductor conductor, DAlterChangeRecord record, boolean applied,
        Transaction transaction) {
        if (record.isApplied() == applied) return null;

        AlterDBChange<?, ?> obj = record.getChangeObj();
        if (applied) obj.redo(transaction);
        else obj.undo(transaction);

        DAlterUndoHistory history = new DAlterUndoHistory(conductor, record, applied);
        record.setApplied(applied);
        record.addHistory(history);
        record.save(transaction);
        history.save(transaction);
        return history;
    }

    interface AlterQueryApi {

        static List<DAlterChangeRecord> findUnAppliedChangesBefore(DAlterChangeRecord alter) {
            AlterDBChange<?, ?> alterObj = alter.getChangeObj();
            return findChangesOnObj(alter, false).stream()
                .filter(o -> alterObj.isDependent(o.getChangeObj()))
                .toList();
        }

        static List<DAlterChangeRecord> findAppliedChangesOnObjAfter(DAlterChangeRecord alter) {
            AlterDBChange<?, ?> alterObj = alter.getChangeObj();
            return findChangesOnObj(alter, true).stream()
                .filter(o -> o.getChangeObj().isDependent(alterObj))
                .toList();
        }

        private static List<DAlterChangeRecord> findChangesOnObj(DAlterChangeRecord alter, boolean filterApplied) {
            return new QDAlterChangeRecord().where()
                .entityId.eq(alter.getEntityId())
                .type.eq(alter.getAlterType())
                .applied.eq(filterApplied)
                .orderBy().eventDate.asc()
                .findList();
        }

        static DAlterChangeRecord findById(long id) {
            return new QDAlterChangeRecord()
                .where().id.eq(id)
                .findOne();
        }
    }

    interface AlterCreateApi {

        static void create(AlterDBCreate<?> create, Transaction transaction) {
        }

        static DAlterChangeRecord applyChange(DStaffConductor staff, AlterDBChange<?, ?> change) {
            DAlterChangeRecord record = new DAlterChangeRecord(change);
            DAlterUndoHistory history = new DAlterUndoHistory(staff, record, true);
            record.addHistory(history);
            try (Transaction transaction = DB.beginTransaction()) {
                record.save(transaction);
                history.save(transaction);
                change.redo(transaction);
                transaction.commit();
            }
            return record;
        }
    }
}
