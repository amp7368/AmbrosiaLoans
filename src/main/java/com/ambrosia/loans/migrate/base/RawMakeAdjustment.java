package com.ambrosia.loans.migrate.base;

import com.ambrosia.loans.database.account.event.investment.InvestApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.database.system.service.SimulationOptions;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;

public interface RawMakeAdjustment {

    Emeralds getBalanceAt(Instant date);

    default void confirm() {
        SimulationOptions options = SimulationOptions.options()
            .setClient(this.client())
            .setEndDate(date().plusMillis(1));
        TemporalAccessor parsed = DiscordModule.SIMPLE_DATE_FORMATTER.parse("09/17/22");
        if (date().isBefore(Instant.from(parsed)))
            RunBankSimulation.simulate(Instant.EPOCH, options);
        client().refresh();
        Instant date = this.date();
        Emeralds realBal = getBalanceAt(date);
        Emeralds difference = this.amount().minus(realBal);
        InvestApi.createAdjustment(difference, this.client(), date, false);
        if (this.client().getId() == 132) {
            System.out.printf("%s real: %s ---- amount: %s ====%s%n", date, realBal, amount(), difference);
        }
        System.out.println("confirm " + this.getId() + " " + date);
    }

    long getId();

    Emeralds amount();

    DClient client();

    Instant date();
}
