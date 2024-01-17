package com.ambrosia.loans.database;

import apple.lib.ebean.database.AppleEbeanDatabaseMetaConfig;
import apple.lib.modules.AppleModule;
import apple.lib.modules.configs.factory.AppleConfigLike;
import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.AmbrosiaDatabase.AmbrosiaDatabaseConfig;
import com.ambrosia.loans.database.entity.client.ClientApi;
import com.ambrosia.loans.database.system.init.ExampleData;
import com.ambrosia.loans.database.system.init.InitDatabase;
import java.util.List;

public class DatabaseModule extends AppleModule {


    @Override
    public void onLoad() {
        AppleEbeanDatabaseMetaConfig.configureMeta(
            Ambrosia.class,
            Ambrosia.get().getDataFolder(),
            logger()::error,
            logger()::info);

        new AmbrosiaDatabase();
        ClientApi.load();
        InitDatabase.init();

        if (AmbrosiaDatabaseConfig.get().isExample())
            ExampleData.loadExample();
    }

    @Override
    public List<AppleConfigLike> getConfigs() {
        return List.of(configJson(AmbrosiaDatabaseConfig.class, "DatabaseConfig", "Config"));
    }

    @Override
    public String getName() {
        return "Database";
    }
}
