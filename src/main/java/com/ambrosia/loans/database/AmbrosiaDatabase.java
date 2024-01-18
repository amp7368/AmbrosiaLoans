package com.ambrosia.loans.database;

import apple.lib.ebean.database.AppleEbeanDatabase;
import apple.lib.ebean.database.config.AppleEbeanDatabaseConfig;
import apple.lib.ebean.database.config.AppleEbeanPostgresConfig;
import com.ambrosia.loans.database.account.balance.DAccountSnapshot;
import com.ambrosia.loans.database.account.event.base.AccountEvent;
import com.ambrosia.loans.database.account.event.invest.DInvest;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.collateral.DCollateral;
import com.ambrosia.loans.database.account.event.loan.payment.DLoanPayment;
import com.ambrosia.loans.database.account.event.loan.section.DLoanSection;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.messages.checkin.DCheckInMessage;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import java.util.Collection;
import java.util.List;

public class AmbrosiaDatabase extends AppleEbeanDatabase {

    @Override
    protected void addEntities(List<Class<?>> entities) {
        // client
        entities.addAll(List.of(ClientDiscordDetails.class, ClientMinecraftDetails.class, DCheckInMessage.class));
        entities.add(DClient.class);
        // staff
        entities.add(DStaffConductor.class);

        // log
        entities.add(AccountEvent.class);
        entities.addAll(List.of(DLoan.class, DLoanSection.class, DLoanPayment.class, DCollateral.class));
        entities.add(DInvest.class);

        // simulation
        entities.add(DAccountSnapshot.class);
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
    }

}
