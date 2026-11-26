package com.ambrosia.loans.discord.base.command.staff;

import com.ambrosia.loans.discord.base.command.BaseSubCommandGroup;

public abstract class BaseStaffSubCommandGroup extends BaseSubCommandGroup {

    public BaseStaffSubCommandGroup() {
        setOnlyEmployee();
    }
}
