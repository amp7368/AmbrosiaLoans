package com.ambrosia.loans.database.bank.query;

public class CachedQueryResult<T> {

    private final Object sync = new Object();
    private boolean isStarted = false;
    private T result;

    public T result() {
        synchronized (sync) {
            if (result != null) return result;
            try {
                sync.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public T result(T newValue) {
        synchronized (sync) {
            result = newValue;
            sync.notifyAll();
        }
        return result;
    }

    /**
     * @return true if the result is being computed
     */
    public boolean start() {
        synchronized (sync) {
            if (isStarted) return true;
            isStarted = true;
            return false;
        }
    }
}
