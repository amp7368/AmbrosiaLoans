package com.ambrosia.loans.discord.base.command;

public record DiscordRestrictions(DiscordRestrictionLevel level, DiscordGuildRestriction guild) {

    public enum DiscordRestrictionLevel {
        MANAGER,
        STAFF,
        PUBLIC
    }

    public enum DiscordGuildRestriction {
        IN_AMBROSIA,
        IN_GUILD,
        ANYWHERE
    }
}
