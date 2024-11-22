package com.ambrosia.loans.database.alter;

import com.ambrosia.loans.database.alter.change.AlterDB;
import com.ambrosia.loans.database.alter.change.AlterDBChange;
import com.ambrosia.loans.database.alter.change.DAlterChange;
import com.ambrosia.loans.database.alter.change.DAlterChangeUndoHistory;
import com.ambrosia.loans.database.alter.change.query.QDAlterChange;
import com.ambrosia.loans.database.alter.create.DAlterCreate;
import com.ambrosia.loans.database.alter.create.DAlterCreateUndoHistory;
import com.ambrosia.loans.database.alter.create.query.QDAlterCreate;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import io.ebean.DB;
import io.ebean.Transaction;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public interface AlterRecordApi {

    @Nullable
    static DAlterChangeUndoHistory undo(DStaffConductor conductor, DAlterChange record) {
        return apply(conductor, record, false);
    }

    @Nullable
    static DAlterChangeUndoHistory redo(DStaffConductor conductor, DAlterChange record) {
        return apply(conductor, record, true);
    }

    @Nullable
    private static DAlterChangeUndoHistory apply(DStaffConductor conductor, DAlterChange record, boolean applied) {
        try (Transaction transaction = DB.beginTransaction()) {
            DAlterChangeUndoHistory history = apply(conductor, record, applied, transaction);
            transaction.commit();
            record.refresh();
            return history;
        }
    }

    @Nullable
    private static DAlterChangeUndoHistory apply(DStaffConductor conductor, DAlterChange record, boolean applied,
        Transaction transaction) {
        if (record.isApplied() == applied) return null;

        AlterDBChange<?, ?> obj = record.getObj();
        if (applied) obj.redo(transaction);
        else obj.undo(transaction);

        DAlterChangeUndoHistory history = new DAlterChangeUndoHistory(conductor, record, applied);
        record.setApplied(applied);
        record.addHistory(history);
        record.save(transaction);
        history.save(transaction);
        return history;
    }

    interface AlterQueryApi {

        static List<DAlterChange> findUnAppliedChangesBefore(DAlterChange alter) {
            AlterDB<?> alterObj = alter.getObj();
            return findChangesOnObj(alter, false).stream()
                .filter(o -> alterObj.isDependent(o.getObj()))
                .toList();
        }

        static List<DAlterChange> findAppliedChangesOnObjAfter(DAlterChange alter) {
            AlterDB<?> alterObj = alter.getObj();
            return findChangesOnObj(alter, true).stream()
                .filter(o -> o.getObj().isDependent(alterObj))
                .toList();
        }

        private static List<DAlterChange> findChangesOnObj(DAlterChange alter, boolean filterApplied) {
            return new QDAlterChange().where()
                .entity.entityId.eq(alter.getEntityId())
                .entity.entityType.eq(alter.getEntityType())
                .applied.eq(filterApplied)
                .orderBy().eventDate.asc()
                .findList();
        }

        static DAlterChange findChangeById(long id) {
            return new QDAlterChange()
                .where().id.eq(id)
                .findOne();
        }

        static DAlterCreate findCreateByEntityId(long entityId, AlterCreateType entityType) {
            return findCreateByEntityId(entityId, entityType.getTypeId());
        }

        static DAlterCreate findCreateByEntityId(long entityId, String entityType) {
            return new QDAlterCreate().where()
                .entityId.eq(entityId)
                .entityType.eq(entityType)
                .findOne();
        }
    }

    interface AlterCreateApi {

        static void create(DStaffConductor staff, AlterCreateType entityType, long entityId) {
            DAlterCreate record = new DAlterCreate(entityType, entityId);
            DAlterCreateUndoHistory history = new DAlterCreateUndoHistory(staff, record, true);
            record.addHistory(history);
            try (Transaction transaction = DB.beginTransaction()) {
                record.save(transaction);
                history.save(transaction);
                transaction.commit();
            }
        }

        static void delete(DStaffConductor staff, DAlterCreate record) {
            DAlterCreateUndoHistory history = new DAlterCreateUndoHistory(staff, record, false);
            record.addHistory(history);
            record.deleteEntity();
            try (Transaction transaction = DB.beginTransaction()) {
                record.save(transaction);
                history.save(transaction);
                transaction.commit();
            }
            RunBankSimulation.simulateAsync(record.getEventDate());
        }

        static DAlterChange applyChange(DStaffConductor staff, AlterDBChange<?, ?> change) {
            DAlterCreate create = AlterQueryApi.findCreateByEntityId(change.getEntityId(), change.getEntityType());
            if (create == null) {
                String msg = "Create{%s,%s} should already exist".formatted(change.getEntityId(),
                    change.getEntityType().displayName());
                DiscordLog.errorSystem(msg);
                throw new IllegalStateException(msg);
            }
            DAlterChange record = new DAlterChange(change, create);
            DAlterChangeUndoHistory history = new DAlterChangeUndoHistory(staff, record, true);
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
