package com.ambrosia.loans.config;

import com.ambrosia.loans.discord.DiscordConfig;
import com.ambrosia.loans.discord.DiscordPermissions;

public class AmbrosiaConfig {

    private static AmbrosiaConfig instance;
    public boolean isProduction = true;
    public DiscordConfig discord = new DiscordConfig();
    public DiscordPermissions discordPermissions = new DiscordPermissions();
    private boolean shouldResimulate = false;

    public AmbrosiaConfig() {
        instance = this;
    }

    public static AmbrosiaConfig get() {
        return instance;
    }

    public static AmbrosiaStaffConfig staff() {
        return AmbrosiaStaffConfig.get();
    }

    public boolean isProduction() {
        return this.isProduction;
    }

    public boolean shouldResimulate() {
        return this.shouldResimulate;
    }
}
