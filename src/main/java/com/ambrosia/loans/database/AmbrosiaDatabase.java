package com.ambrosia.loans.database;

import apple.lib.modules.AppleModule;
import apple.lib.modules.configs.data.config.AppleConfig.Builder;
import apple.lib.modules.configs.factory.AppleConfigLike;
import com.ambrosia.loans.Ambrosia;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import java.io.File;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class AmbrosiaDatabase extends AppleModule {

    private File databaseConfigFile;

    @Override
    public void onLoad() {
        if (!AmbrosiaDatabaseConfig.get().isConfigured()) {
            this.logger().fatal("Please configure " + this.databaseConfigFile.getAbsolutePath());
            System.exit(1);
        }
        DataSourceConfig dataSourceConfig = configureDataSource(AmbrosiaDatabaseConfig.get());
        DatabaseConfig dbConfig = configureDatabase(dataSourceConfig);
        DatabaseFactory.createWithContextClassLoader(dbConfig, Ambrosia.class.getClassLoader());
        logger().info("Successfully created database");
    }

    @NotNull
    private static DatabaseConfig configureDatabase(DataSourceConfig dataSourceConfig) {
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setDataSourceConfig(dataSourceConfig);
        dbConfig.setDdlGenerate(true);
        dbConfig.setDdlRun(AmbrosiaDatabaseConfig.get().getDDLRun());

        // tables
        // embedded
        return dbConfig;
    }

    @NotNull
    private static DataSourceConfig configureDataSource(AmbrosiaDatabaseConfig loadedConfig) {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setUsername(loadedConfig.getUsername());
        dataSourceConfig.setPassword(loadedConfig.getPassword());
        dataSourceConfig.setUrl(loadedConfig.getUrl());
        dataSourceConfig.setAutoCommit(true);
        return dataSourceConfig;
    }

    @Override
    public List<AppleConfigLike> getConfigs() {
        Builder<AmbrosiaDatabaseConfig> databaseConfig = configJson(AmbrosiaDatabaseConfig.class, "Database.config", "Config");
        this.databaseConfigFile = this.getFile("Config", "Database.config.json");
        return List.of(databaseConfig);
    }

    @Override
    public String getName() {
        return "Database";
    }
}
