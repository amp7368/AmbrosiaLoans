package com.ambrosia.loans.discord.system.help;

import apple.utilities.util.Pretty;

public enum HelpCommandListType {
    MANAGER,
    STAFF,
    CLIENT,
    ALL;

    public static HelpCommandListType getType(boolean isManagerCommand, boolean isStaffCommand) {
        if (isManagerCommand) return MANAGER;
        if (isStaffCommand) return STAFF;
        return CLIENT;
    }

    public HelpCommandList getList() {
        return switch (this) {
            case MANAGER -> HelpCommandListManager.getManager();
            case STAFF -> HelpCommandListManager.getStaff();
            case CLIENT -> HelpCommandListManager.getClient();
            case ALL -> HelpCommandListManager.getAll();
        };
    }

    public String display() {
        return Pretty.spaceEnumWords(this.name());
    }
}
