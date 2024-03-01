package com.ambrosia.loans;

import com.ambrosia.loans.discord.DiscordConfig;
import com.ambrosia.loans.discord.DiscordPermissions;

public class AmbrosiaConfig {

    private static AmbrosiaConfig instance;
    public boolean isProduction = true;
    public DiscordConfig discord = new DiscordConfig();
    public DiscordPermissions discordPermissions = new DiscordPermissions();

    public AmbrosiaConfig() {
        instance = this;
    }

    public static AmbrosiaConfig get() {
        return instance;
    }

    public boolean isProduction() {
        return this.isProduction;
    }
}
