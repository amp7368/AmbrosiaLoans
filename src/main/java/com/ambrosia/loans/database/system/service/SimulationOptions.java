package com.ambrosia.loans.database.system.service;

import com.ambrosia.loans.database.entity.client.DClient;
import java.time.Instant;
import java.util.Objects;

public class SimulationOptions {

    private DClient client;
    private Instant endDate;

    public static SimulationOptions options() {
        return new SimulationOptions();
    }

    public Instant getEndDate() {
        return Objects.requireNonNullElseGet(endDate, Instant::now);
    }

    /**
     * only used for imports!!!
     *
     * @param endDate the date to simulate to
     * @return this
     */

    public SimulationOptions setEndDate(Instant endDate) {
        this.endDate = endDate;
        return this;
    }

    public DClient getClient() {
        return client;
    }

    public SimulationOptions setClient(DClient client) {
        this.client = client;
        return this;
    }

    public boolean hasClient() {
        return false;
        // todo        return this.client != null;
    }
}
