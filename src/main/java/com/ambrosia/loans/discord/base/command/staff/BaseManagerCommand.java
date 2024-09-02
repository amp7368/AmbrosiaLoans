package com.ambrosia.loans.discord.base.command.staff;

public abstract class BaseManagerCommand extends BaseStaffCommand {

    @Override
    public boolean isOnlyManager() {
        return true;
    }
}
