package com.ambrosia.loans.database.config.model;


import com.ambrosia.loans.database.system.AppDBEnum;

public enum ConfigUpdateType implements AppDBEnum {
    CREATE,
    SCHEMA_UPDATE,
    UPDATE,
    FIX,
    DELETE,
    RESTORE;

    public static final String DEFINITION = "config_update_type";

    @Override
    public String getDBValue() {
        return name();
    }
}
