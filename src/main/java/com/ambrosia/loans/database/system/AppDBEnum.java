package com.ambrosia.loans.database.system;

import io.ebean.annotation.DbEnumValue;

public interface AppDBEnum {

    @DbEnumValue(withConstraint = false)
    String getDBValue();
}
