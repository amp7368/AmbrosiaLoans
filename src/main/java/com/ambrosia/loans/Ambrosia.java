package com.ambrosia.loans;

import apple.lib.modules.AppleModule;
import apple.lib.modules.ApplePlugin;
import apple.lib.modules.configs.factory.AppleConfigLike;
import com.ambrosia.loans.config.AmbrosiaConfig;
import com.ambrosia.loans.config.AmbrosiaStaffConfig;
import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.service.ServiceModule;
import java.util.List;

public class Ambrosia extends ApplePlugin {

    private static Ambrosia instance;

    public Ambrosia() {
        instance = this;
    }

    public static void main(String[] args) {
        new Ambrosia().start();
    }

    public static Ambrosia get() {
        return instance;
    }


    @Override
    public List<AppleModule> createModules() {
        return List.of(new DatabaseModule(), new DiscordModule(), new ServiceModule());
    }

    @Override
    public List<AppleConfigLike> getConfigs() {
        return List.of(
            configJson(AmbrosiaConfig.class, "AmbrosiaConfig"),
            configJson(AmbrosiaStaffConfig.class, "AmbrosiaStaffConfig")
        );
    }

    @Override
    public String getName() {
        return "AmbrosiaLoans";
    }
}
