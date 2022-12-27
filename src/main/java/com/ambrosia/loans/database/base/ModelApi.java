package com.ambrosia.loans.database.base;

import com.ambrosia.loans.database.util.CreateEntityException;
import com.ambrosia.loans.database.util.UniqueMessages;
import io.ebean.Model;

public abstract class ModelApi<T extends Model> {

    public final T entity;

    public ModelApi(T entity) {
        this.entity = entity;
    }

    public void update() {
    }

    public boolean trySave() {
        final T entity = this.getEntity();
        try {
            UniqueMessages.saveIfUnique(entity);
            update();
            return true;
        } catch (CreateEntityException e) {
            return false;
        }
    }

    public boolean isEmpty() {
        return this.getEntity() == null;
    }

    public T getEntity() {
        return this.entity;
    }
}
