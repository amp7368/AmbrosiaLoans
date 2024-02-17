package com.ambrosia.loans.database.alter.base;

import com.ambrosia.loans.database.alter.gson.AlterRecordType;

public abstract class AlterDBCreate<Entity> extends AlterDB<Entity> {

    String entity;

    public AlterDBCreate() {
    }

    public AlterDBCreate(AlterRecordType typeId, Entity entity, long entityId) {
        super(typeId, entityId);
        this.entity = gson().toJson(entity);
    }

    @Override
    public String getEntityTypeName() {
        return "Client";
    }

    /**
     * Be very careful using this deserialization.
     * Should not be saved, otherwise all other actions will be overwritten
     *
     * @return the deserialized version of this Entity
     */
    protected final Entity create() {
        return gson().fromJson(entity, entityClass());
    }

    protected abstract Class<Entity> entityClass();
}
