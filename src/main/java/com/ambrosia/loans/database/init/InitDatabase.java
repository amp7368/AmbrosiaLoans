package com.ambrosia.loans.database.init;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;

public class InitDatabase {

    public static void init() {
        DStaffConductor.insertDefaultConductors();
    }
}
