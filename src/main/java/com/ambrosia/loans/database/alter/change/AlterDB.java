package com.ambrosia.loans.database.alter.change;

import com.ambrosia.loans.database.alter.type.AlterChangeType;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.alter.type.AlterGson;
import com.google.gson.Gson;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public abstract class AlterDB<Entity> {

    private AlterChangeType typeId;

    private transient long entityId;

    private transient Instant appliedDate;
    private transient EnumSet<AlterImpactedField> impactedFields;
    private transient boolean isApplied;


    public AlterDB() {
    }

    public AlterDB(AlterChangeType typeId, long entityId) {
        this.typeId = typeId;
        this.appliedDate = Instant.now();
        this.entityId = entityId;
    }

    public void init(DAlterChange record) {
        this.entityId = record.getEntityId();
        this.appliedDate = record.getEventDate();
        this.isApplied = record.isApplied();
    }

    public long getEntityId() {
        return entityId;
    }

    public final Instant getAppliedDate() {
        return appliedDate;
    }

    public final boolean isApplied() {
        return isApplied;
    }

    public AlterChangeType getType() {
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

    public boolean isDependent(AlterDB<?> dependency) {
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

    protected boolean isDependentInternal(AlterDB<?> dependency) {
        return true;
    }

    public abstract AlterCreateType getEntityType();
}
