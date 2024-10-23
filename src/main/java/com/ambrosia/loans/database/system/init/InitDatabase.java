package com.ambrosia.loans.database.system.init;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.version.DApiVersion;
import com.ambrosia.loans.database.version.investor.DVersionInvestorCap;

public class InitDatabase {

    public static void init() {
        DStaffConductor.insertDefaultConductors();
        DApiVersion.initVersions();
        DVersionInvestorCap.initVersions();
    }
}
