package com.ambrosia.loans.database.alter.gson;

import apple.utilities.util.Pretty;

public enum AlterCreateType {
    CLIENT("CLIENT"),
    LOAN("LOAN"),
    ADJUST_LOAN("ADJUST_LOAN"),
    PAYMENT("PAYMENT"),
    INVEST("INVEST"),
    ADJUST_BALANCE("ADJUST_BALANCE"),
    WITHDRAWAL("WITHDRAWAL");

    private final String typeName;

    AlterCreateType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeId() {
        return typeName;
    }

    public String displayName() {
        return Pretty.spaceEnumWords(this.name());
    }
}
