package com.ambrosia.loans.database;

import apple.lib.ebean.database.AppleEbeanDatabase;
import apple.lib.ebean.database.config.AppleEbeanDatabaseConfig;
import com.ambrosia.loans.database.client.ClientDiscordDetails;
import com.ambrosia.loans.database.client.ClientMinecraftDetails;
import com.ambrosia.loans.database.client.ClientMoment;
import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.database.loan.DLoan;
import com.ambrosia.loans.database.loan.collateral.DCollateral;
import com.ambrosia.loans.database.loan.payment.DLoanPayment;
import com.ambrosia.loans.database.loan.section.DLoanSection;
import com.ambrosia.loans.database.messages.checkin.DCheckInMessage;
import com.ambrosia.loans.database.transaction.DTransaction;
import java.util.Collection;
import java.util.List;

public class AmbrosiaDatabase extends AppleEbeanDatabase {

    @Override
    protected void addEntities(List<Class<?>> entities) {
        // client
        entities.addAll(List.of(ClientMoment.class, ClientDiscordDetails.class, ClientMinecraftDetails.class));
        entities.add(DClient.class);
        
        // transaction
        entities.add(DTransaction.class);
        // loan
        entities.addAll(List.of(DLoan.class, DLoanSection.class, DLoanPayment.class, DCollateral.class, DCheckInMessage.class));
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
}
