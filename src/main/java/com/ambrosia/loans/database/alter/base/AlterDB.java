package com.ambrosia.loans.database.alter.base;

import com.ambrosia.loans.database.alter.db.DAlterChangeRecord;
import com.ambrosia.loans.database.alter.gson.AlterGson;
import com.ambrosia.loans.database.alter.gson.AlterRecordType;
import com.google.gson.Gson;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public abstract class AlterDB<Entity> {

    private AlterRecordType typeId;

    private transient long entityId;

    private transient Instant appliedDate;
    private transient EnumSet<AlterImpactedField> impactedFields;


    public AlterDB() {
    }

    public AlterDB(AlterRecordType typeId, long entityId) {
        this.typeId = typeId;
        this.appliedDate = Instant.now();
        this.entityId = entityId;
    }

    public void init(DAlterChangeRecord record) {
        this.entityId = record.getEntityId();
        this.appliedDate = record.getEventDate();
    }

    public long getEntityId() {
        return entityId;
    }

    public final Instant getAppliedDate() {
        return appliedDate;
    }

    public AlterRecordType getType() {
        return this.typeId;
    }

    @NotNull
    public String toJson() {
        return gson().toJson(this);
    }

    protected Gson gson() {
        return AlterGson.gson();
    }

    public abstract Entity getEntity();

    public boolean isDependent(AlterDBChange<?, ?> dependency) {
        boolean thisIsAfter = dependency.getAppliedDate().isBefore(this.getAppliedDate());

        boolean hasOverlappingFields = false;
        for (AlterImpactedField field : this.impactedFields()) {
            if (dependency.impactedFields().contains(field))
                hasOverlappingFields = true;
        }
        return thisIsAfter && hasOverlappingFields && isDependentInternal(dependency);
    }

    public final Set<AlterImpactedField> impactedFields() {
        if (this.impactedFields != null) return impactedFields;
        return this.impactedFields = EnumSet.copyOf(initImpactedFields());
    }

    protected abstract Collection<AlterImpactedField> initImpactedFields();

    protected boolean isDependentInternal(AlterDBChange<?, ?> dependency) {
        return true;
    }

    public abstract String getEntityTypeName();
}
