package com.ambrosia.loans.database.alter.change;

import com.ambrosia.loans.database.alter.type.AlterChangeType;
import io.ebean.Transaction;

public abstract class AlterDBChange<Entity, T> extends AlterDB<Entity> {

    private T current;
    private T previous;

    public AlterDBChange() {
    }

    public AlterDBChange(AlterChangeType typeId, long entityId,
        T previous, T current) {
        super(typeId, entityId);
        this.previous = previous;
        this.current = current;
    }

    public final T getCurrent() {
        return current;
    }

    public final T getPrevious() {
        return previous;
    }

    public final void undo(Transaction transaction) {
        this.apply(getEntity(), getPrevious(), transaction);
    }

    public final void redo(Transaction transaction) {
        this.apply(getEntity(), getCurrent(), transaction);
    }

    protected abstract void apply(Entity entity, T value, Transaction transaction);
}
