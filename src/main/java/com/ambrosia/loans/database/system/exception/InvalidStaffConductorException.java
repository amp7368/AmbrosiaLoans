package com.ambrosia.loans.database.system.exception;

public class InvalidStaffConductorException extends Exception {

    private final String username;
    private final long discordId;

    public InvalidStaffConductorException(String username, long discordId) {
        super("%s (%d) is not a valid client".formatted(username, discordId));
        this.username = username;
        this.discordId = discordId;
    }

}
