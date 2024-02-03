package com.ambrosia.loans.database.version;


import io.ebean.annotation.DbEnumValue;

public enum VersionEntityType {
    LOAN;

    @DbEnumValue(withConstraint = false)
    public String id() {
        return name();
    }
}
