package com.ambrosia.loans.database.alter;

import com.ambrosia.loans.database.alter.base.AlterDBChange;
import com.ambrosia.loans.database.alter.base.AlterDBCreate;
import com.ambrosia.loans.database.alter.db.DAlterChangeRecord;
import com.ambrosia.loans.database.alter.db.DAlterUndoHistory;
import com.ambrosia.loans.database.alter.db.query.QDAlterChangeRecord;
import com.ambrosia.loans.database.alter.gson.AlterRecordType;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Instant;
import java.util.List;

public interface AlterRecordApi {

    static DAlterUndoHistory undo(DStaffConductor conductor, DAlterChangeRecord record) {
        try (Transaction transaction = DB.beginTransaction()) {
            DAlterUndoHistory history = undo(conductor, record, transaction);
            transaction.commit();
            return history;
        }
    }

    static DAlterUndoHistory undo(DStaffConductor conductor, DAlterChangeRecord record, Transaction transaction) {
        if (!record.isApplied()) return null;

        record.getChangeObj().undo(transaction);

        DAlterUndoHistory history = new DAlterUndoHistory(conductor, record, false);
        record.setApplied(false);
        record.addHistory(history);
        record.save(transaction);
        history.save(transaction);
        return history;
    }

    interface AlterQueryApi {

        static List<DAlterChangeRecord> findAppliedChangesOnObjAfter(DAlterChangeRecord alter) {
            Instant date = alter.getEventDate();
            AlterDBChange<?, ?> alterObj = alter.getChangeObj();
            return findAppliedChangesOnObjAfter(alter.getEntityId(), alter.getAlterType())
                .stream()
                .filter(o -> o.getChangeObj().isDependent(alterObj))
                .toList();
        }

        private static List<DAlterChangeRecord> findAppliedChangesOnObjAfter(long entityId, AlterRecordType type) {
            return new QDAlterChangeRecord().where()
                .entityId.eq(entityId)
                .type.eq(type)
                .applied.isTrue()
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

        static void change(DStaffConductor staff, AlterDBChange<?, ?> change, Transaction transaction) {
            DAlterChangeRecord record = new DAlterChangeRecord(change);
            DAlterUndoHistory history = new DAlterUndoHistory(staff, record, true);
            record.addHistory(history);
            record.save(transaction);
            history.save(transaction);
        }
    }
}
