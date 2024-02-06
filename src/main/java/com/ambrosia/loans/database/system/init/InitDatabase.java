package com.ambrosia.loans.database.system.init;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.version.DApiVersion;

public class InitDatabase {

    public static void init() {
        DStaffConductor.insertDefaultConductors();
        DApiVersion.initVersions();
    }
}
