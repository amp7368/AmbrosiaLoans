package com.ambrosia.loans.database;

import apple.lib.ebean.database.AppleEbeanDatabase;
import apple.lib.ebean.database.config.AppleEbeanDatabaseConfig;
import apple.lib.ebean.database.config.AppleEbeanPostgresConfig;
import com.ambrosia.loans.database.account.DClientSnapshot;
import com.ambrosia.loans.database.account.adjust.DAdjustBalance;
import com.ambrosia.loans.database.account.adjust.DAdjustLoan;
import com.ambrosia.loans.database.account.base.AccountEvent;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.collateral.DCollateral;
import com.ambrosia.loans.database.account.investment.DInvestment;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.section.DLoanSection;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.account.withdrawal.DWithdrawal;
import com.ambrosia.loans.database.alter.change.DAlterChange;
import com.ambrosia.loans.database.alter.change.DAlterChangeUndoHistory;
import com.ambrosia.loans.database.alter.create.DAlterCreate;
import com.ambrosia.loans.database.alter.create.DAlterCreateUndoHistory;
import com.ambrosia.loans.database.bank.DBankSnapshot;
import com.ambrosia.loans.database.config.model.ConfigUpdateType;
import com.ambrosia.loans.database.config.model.DConfig;
import com.ambrosia.loans.database.config.model.DConfigType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.settings.DClientMessagingSettings;
import com.ambrosia.loans.database.entity.client.settings.DClientSettings;
import com.ambrosia.loans.database.entity.client.username.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.username.ClientMinecraftDetails;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.message.DClientMessage;
import com.ambrosia.loans.database.message.comment.DComment;
import com.ambrosia.loans.database.message.log.DCommandLog;
import com.ambrosia.loans.database.message.log.DLog;
import com.ambrosia.loans.database.system.AppDBEnum;
import com.ambrosia.loans.database.version.DApiVersion;
import com.ambrosia.loans.database.version.investor.DVersionInvestorCap;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.ebean.DB;
import io.ebean.config.AutoTuneConfig;
import io.ebean.config.AutoTuneMode;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.dbmigration.DbMigration;
import io.ebean.migration.MigrationConfig;
import java.sql.SQLSyntaxErrorException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AmbrosiaDatabase extends AppleEbeanDatabase {

    private static AmbrosiaDatabase instance;
    private ObjectMapper objectMapper;

    public AmbrosiaDatabase() {
        instance = this;
    }

    public static AmbrosiaDatabase get() {
        return instance;
    }

    @Override
    protected DatabaseConfig configureDatabase(DataSourceConfig dataSourceConfig) {
        DatabaseConfig databaseConfig = super.configureDatabase(dataSourceConfig);
        AutoTuneConfig autoTune = databaseConfig.getAutoTuneConfig();
        autoTune.setProfiling(true);
        autoTune.setQueryTuning(true);
        autoTune.setMode(AutoTuneMode.DEFAULT_ON);
        return databaseConfig;
    }

    @Override
    protected void configureMigration(DbMigration migration, MigrationConfig config) {
        List<AppDBEnum[]> enums = List.of(AccountEventType.values(), ConfigUpdateType.values());
        List<String> enumDefinitions = List.of(AccountEventType.DEFINITION, ConfigUpdateType.DEFINITION);
        for (int i = 0; i < enums.size(); i++) {
            AppDBEnum[] enumValues = enums.get(i);
            String enumDefinition = enumDefinitions.get(i);

            checkEnumValuesInMigration(enumValues);
            String enumValuesStr = Arrays.stream(enumValues)
                .map(AppDBEnum::getDBValue)
                .map("'%s'"::formatted)
                .collect(Collectors.joining(", "));
            String updateStatement = """
                DO $$ BEGIN
                    CREATE TYPE %s AS ENUM (%s);
                    CREATE CAST (VARCHAR AS %s) WITH INOUT AS IMPLICIT;
                EXCEPTION
                    WHEN duplicate_object THEN null;
                END $$;
                """.formatted(enumDefinition, enumValuesStr, enumDefinition);
            DB.sqlUpdate(updateStatement).executeNow();
        }
    }

    @Override
    protected boolean isDefault() {
        return true;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        if (this.objectMapper != null) return objectMapper;

        objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return objectMapper;
    }

    @Override
    protected void addEntities(List<Class<?>> entities) {
        // client
        entities.addAll(List.of(DClientSettings.class, DClientMessagingSettings.class,
            ClientDiscordDetails.class, ClientMinecraftDetails.class));
        entities.add(DClient.class);
        // staff
        entities.add(DStaffConductor.class);

        entities.add(AccountEventType.class);
        // log
        entities.add(AccountEvent.class);
        entities.addAll(List.of(DLoan.class, DLoanSection.class, DLoanPayment.class, DCollateral.class));
        entities.addAll(List.of(DInvestment.class, DWithdrawal.class, DAdjustBalance.class, DAdjustLoan.class));

        // alter
        entities.addAll(List.of(DAlterChange.class, DAlterChangeUndoHistory.class));
        entities.addAll(List.of(DAlterCreate.class, DAlterCreateUndoHistory.class));

        // message
        entities.add(DClientMessage.class);
        entities.add(DComment.class);
        entities.add(DLog.class);
        entities.add(DCommandLog.class);

        // simulation
        entities.add(DClientSnapshot.class);

        // misc
        entities.add(DApiVersion.class);
        entities.add(DVersionInvestorCap.class);
        entities.add(DBankSnapshot.class);
        entities.addAll(List.of(DConfigType.class, DConfig.class));
    }

    @Override
    protected Collection<Class<?>> getQueryBeans() {
        return List.of();
    }

    @Override
    protected AppleEbeanDatabaseConfig getConfig() {
        return AmbrosiaDatabaseConfig.get();
    }

    @Override
    protected String getName() {
        return "Ambrosia";
    }

    private void checkEnumValuesInMigration(AppDBEnum[] enumValues) throws RuntimeException {
        for (AppDBEnum value : enumValues) {
            String dbValue = value.getDBValue();
            if (dbValue.contains("'")) {
                String msg = dbValue + " contains an \"'\", and that's not allowed trying to create SQL enums.";
                throw new RuntimeException(new SQLSyntaxErrorException(msg));
            }
        }
    }

    public static class AmbrosiaDatabaseConfig extends AppleEbeanPostgresConfig {

        private static AmbrosiaDatabaseConfig instance;

        protected boolean createExampleData = false;

        public AmbrosiaDatabaseConfig() {
            instance = this;
        }

        public static AmbrosiaDatabaseConfig get() {
            return instance;
        }

        public boolean isExample() {
            return this.createExampleData;
        }
    }
}
