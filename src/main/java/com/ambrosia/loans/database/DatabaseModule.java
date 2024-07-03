package com.ambrosia.loans.database;

import apple.lib.ebean.database.AppleEbeanDatabaseMetaConfig;
import apple.lib.modules.AppleModule;
import apple.lib.modules.configs.factory.AppleConfigLike;
import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.config.AmbrosiaConfig;
import com.ambrosia.loans.database.AmbrosiaDatabase.AmbrosiaDatabaseConfig;
import com.ambrosia.loans.database.system.init.ExampleData;
import com.ambrosia.loans.database.system.init.InitDatabase;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import java.time.Instant;
import java.util.List;

public class DatabaseModule extends AppleModule {


    private static DatabaseModule instance;

    public DatabaseModule() {
        instance = this;
    }

    public static DatabaseModule get() {
        return instance;
    }

    @Override
    public void onLoad() {
        AppleEbeanDatabaseMetaConfig.configureMeta(
            Ambrosia.class,
            Ambrosia.get().getDataFolder(),
            logger()::error,
            logger()::info);

        new AmbrosiaDatabase();
        InitDatabase.init();

        if (AmbrosiaConfig.get().shouldResimulate())
            RunBankSimulation.simulate(Instant.EPOCH);
        if (AmbrosiaDatabaseConfig.get().isExample())
            ExampleData.loadExample();
    }

    @Override
    public List<AppleConfigLike> getConfigs() {
        return List.of(configJson(AmbrosiaDatabaseConfig.class, "DatabaseConfig"));
    }

    @Override
    public String getName() {
        return "Database";
    }
}
