package com.ambrosia.loans.database.system.service;

import java.time.Instant;
import java.util.Objects;

public class SimulationOptions {

    public static final SimulationOptions DEFAULT = options();
    private Instant endDate;

    public static SimulationOptions options() {
        return new SimulationOptions();
    }

    public Instant getEndDate() {
        return Objects.requireNonNullElseGet(endDate, () -> Instant.now().plusSeconds(1));
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
}
