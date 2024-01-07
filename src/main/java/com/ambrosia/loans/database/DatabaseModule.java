package com.ambrosia.loans.database;

import apple.lib.ebean.database.AppleEbeanDatabaseMetaConfig;
import apple.lib.modules.AppleModule;
import apple.lib.modules.configs.factory.AppleConfigLike;
import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.client.ClientApi;
import java.util.List;
import org.apache.logging.log4j.Logger;

public class DatabaseModule extends AppleModule {


    @Override
    public void onLoad() {
        Logger logger = Ambrosia.get().logger();
        AppleEbeanDatabaseMetaConfig.configureMeta(
            Ambrosia.class,
            Ambrosia.get().getDataFolder(),
            logger::error,
            logger::info);
        new AmbrosiaDatabase();
        ClientApi.load();
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
