package com.ambrosia.loans.database;

import apple.lib.ebean.database.AppleEbeanDatabase;
import apple.lib.ebean.database.config.AppleEbeanDatabaseConfig;
import apple.lib.ebean.database.config.AppleEbeanPostgresConfig;
import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.account.balance.DClientSnapshot;
import com.ambrosia.loans.database.account.event.adjust.DAdjustBalance;
import com.ambrosia.loans.database.account.event.adjust.DAdjustLoan;
import com.ambrosia.loans.database.account.event.base.AccountEvent;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.account.event.investment.DInvestment;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.collateral.DCollateral;
import com.ambrosia.loans.database.account.event.loan.section.DLoanSection;
import com.ambrosia.loans.database.account.event.payment.DLoanPayment;
import com.ambrosia.loans.database.account.event.withdrawal.DWithdrawal;
import com.ambrosia.loans.database.alter.db.DAlterChangeRecord;
import com.ambrosia.loans.database.alter.db.DAlterUndoHistory;
import com.ambrosia.loans.database.bank.DBankSnapshot;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.message.DCheckInMessage;
import com.ambrosia.loans.database.message.DComment;
import com.ambrosia.loans.database.version.DApiVersion;
import io.ebean.DB;
import io.ebean.config.AutoTuneConfig;
import io.ebean.config.AutoTuneMode;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.dbmigration.DbMigration;
import io.ebean.migration.MigrationConfig;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AmbrosiaDatabase extends AppleEbeanDatabase {

    private static final String ACCOUNT_EVENT_ENUM;

    static {
        String enumValues = Arrays.stream(AccountEventType.values())
            .map(AccountEventType::getDBValue)
            .map("'%s'"::formatted)
            .collect(Collectors.joining(", "));
        ACCOUNT_EVENT_ENUM = """
            DO $$ BEGIN
                CREATE TYPE %s AS ENUM (%s);
                CREATE CAST (VARCHAR AS EVENT_TYPE) WITH INOUT AS IMPLICIT;
            EXCEPTION
                WHEN duplicate_object THEN null;
            END $$;
            """.formatted(AccountEventType.DEFINITION, enumValues);
    }

    @Override
    protected void addEntities(List<Class<?>> entities) {
        // client
        entities.addAll(List.of(ClientDiscordDetails.class, ClientMinecraftDetails.class));
        entities.add(DClient.class);
        // staff
        entities.add(DStaffConductor.class);

        entities.add(AccountEventType.class);
        // log
        entities.add(AccountEvent.class);
        entities.addAll(List.of(DLoan.class, DLoanSection.class, DLoanPayment.class, DCollateral.class));
        entities.addAll(List.of(DInvestment.class, DWithdrawal.class, DAdjustBalance.class, DAdjustLoan.class));

        // alter
        entities.addAll(List.of(DAlterChangeRecord.class, DAlterUndoHistory.class));

        // message
        entities.add(DCheckInMessage.class);
        entities.add(DComment.class);

        // simulation
        entities.add(DClientSnapshot.class);

        // misc
        entities.add(DApiVersion.class);
        entities.add(DBankSnapshot.class);
    }

    @Override
    protected boolean isDefault() {
        return true;
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
        DB.sqlUpdate(ACCOUNT_EVENT_ENUM).executeNow();
    }

    @Override
    protected String getName() {
        return "Ambrosia";
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

        @Override
        public boolean isStrictMode() {
            return Ambrosia.isProduction();
        }
    }

}
