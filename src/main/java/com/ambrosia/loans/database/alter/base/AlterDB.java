package com.ambrosia.loans.database.alter.base;

import com.ambrosia.loans.database.alter.db.DAlterChangeRecord;
import com.ambrosia.loans.database.alter.gson.AlterGson;
import com.ambrosia.loans.database.alter.gson.AlterRecordType;
import com.google.gson.Gson;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public abstract class AlterDB<Entity> {

    private AlterRecordType typeId;

    private transient long entityId;

    private transient Instant appliedDate;


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
    public String serialize() {
        return gson().toJson(this);
    }

    protected Gson gson() {
        return AlterGson.gson();
    }

    public abstract Entity getEntity();

}
