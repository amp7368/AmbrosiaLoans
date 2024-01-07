package com.ambrosia.loans.database;

import apple.lib.ebean.database.config.AppleEbeanPostgresConfig;

public class AmbrosiaDatabaseConfig extends AppleEbeanPostgresConfig {

    private static AmbrosiaDatabaseConfig instance;

    protected boolean createExampleData = false;

    public AmbrosiaDatabaseConfig() {
        instance = this;
    }

    public static AmbrosiaDatabaseConfig get() {
        return instance;
    }

    public boolean isExample() {
        return this.createExampleData;
    }
}
