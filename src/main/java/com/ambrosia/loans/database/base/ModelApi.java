package com.ambrosia.loans.database.base;

import com.ambrosia.loans.database.base.util.CreateEntityException;
import com.ambrosia.loans.database.base.util.UniqueMessages;
import io.ebean.DB;
import io.ebean.Model;

public abstract class ModelApi<T extends Model> {

    public final T entity;

    public ModelApi(T entity) {
        this.entity = entity;
    }

    public void onUpdate() {
    }

    public boolean trySave() {
        try {
            UniqueMessages.saveIfUnique(entity);
            onUpdate();
            return true;
        } catch (CreateEntityException e) {
            return false;
        }
    }

    public void save() {
        DB.save(getEntity());
    }

    public boolean isEmpty() {
        return this.getEntity() == null;
    }

    public T getEntity() {
        return this.entity;
    }

    public void delete() {
        DB.delete(entity);
    }

    public void refresh() {
        DB.refresh(entity);
    }
}
