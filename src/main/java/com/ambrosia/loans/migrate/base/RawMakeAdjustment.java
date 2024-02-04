package com.ambrosia.loans.migrate.base;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.database.system.service.SimulationOptions;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.migrate.ImportModule;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;

public interface RawMakeAdjustment {

    Emeralds getBalanceAt(Instant date);

    default void confirm(Instant lastDate) {
        SimulationOptions options = SimulationOptions.options()
            .setEndDate(date());
        Instant startDate = ImportModule.get().isQuick() ? lastDate : Instant.EPOCH; // todo maybe can be lastDate.minusSeconds(1)
        RunBankSimulation.simulate(startDate, options);
        client().refresh();
        Instant date = this.date();
//        18.84 => 20.48
        // real balance
        // balance in the database
        // balance
        Emeralds realBal = getBalanceAt(date);
        Instant cDate = Instant.from(DiscordModule.SIMPLE_DATE_FORMATTER.parse("07/16/22"));
        if (client().getId() == 132 && date.isBefore(cDate)) {
            System.out.println(this.amount());
            System.out.println(realBal);
        }
        Emeralds difference = this.amount().minus(realBal);
        createAdjustment(difference, date);
    }

    void createAdjustment(Emeralds difference, Instant date);

    long getId();

    Emeralds amount();

    DClient client();

    Instant date();
}
