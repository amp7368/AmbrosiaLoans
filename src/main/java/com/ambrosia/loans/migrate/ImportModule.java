package com.ambrosia.loans.migrate;

import apple.lib.modules.AppleModule;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.system.init.ExampleData;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.migrate.base.ImportedData;
import com.ambrosia.loans.migrate.client.ImportedClient;
import com.ambrosia.loans.migrate.client.RawClient;
import com.ambrosia.loans.migrate.investment.ImportedInvestment;
import com.ambrosia.loans.migrate.investment.RawInvestment;
import com.ambrosia.loans.migrate.loan.ImportedLoan;
import com.ambrosia.loans.migrate.loan.RawLoan;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ImportModule extends AppleModule {

    private static ImportModule instance;

    public ImportModule() {
        instance = this;
    }

    public static ImportModule get() {
        return instance;
    }

    public static List<DClient> toDBClients(ImportRawData rawData) {
        List<ImportedClient> clients = rawData.clients().stream()
            .map(RawClient::convert).toList();
        rawData.loans().forEach(loan -> loan.setClient(clients));
        rawData.investments().forEach(invest -> invest.setClient(clients));

        return clients.stream()
            .map(ImportedData::toDB)
            .toList();
    }


    public static List<DLoan> toDBLoans(ImportRawData rawData) {
        return rawData.loans().parallelStream()
            .map(RawLoan::convert)
            .map(ImportedLoan::toDB)
            .toList();
    }

    private static List<RawInvestment> toDBInvestments(ImportRawData rawData) {
        Comparator<RawInvestment> comparator = Comparator.comparing(RawInvestment::getClientId)
            .thenComparing(RawInvestment::getDate);
        List<RawInvestment> investments = rawData.investments().stream()
            .sorted(comparator)
            .toList();
        List<ImportedInvestment> imported = new ArrayList<>();
        List<RawInvestment> group = new ArrayList<>();
        long clientId = 0;
        for (RawInvestment raw : investments) {
            if (raw.getClientId() != clientId) {
                if (!group.isEmpty()) {
                    clientId = raw.getClientId();
                    imported.add(new ImportedInvestment(group));
                    group = new ArrayList<>();
                }
            }
            group.add(raw);
        }
        imported.forEach(ImportedInvestment::toDB);
        List<RawInvestment> confirms = new ArrayList<>();
        imported.stream()
            .map(ImportedInvestment::confirmList)
            .forEach(confirms::addAll);
        confirms.sort(Comparator.comparing(RawInvestment::getDate));
        return confirms;
    }

    @Override
    public void onEnable() {
        if (!shouldEnable()) return;
        if (shouldReset()) ExampleData.resetData();

        ImportRawData rawData = ImportRawData.loadData();
        List<DClient> clients = toDBClients(rawData);

        List<DLoan> loans = toDBLoans(rawData);

        List<RawInvestment> confirms = toDBInvestments(rawData);
        RunBankSimulation.simulateFromDate(Instant.EPOCH);

        confirms.forEach(RawInvestment::confirm);
        RunBankSimulation.simulateFromDate(Instant.EPOCH);

        printLoans(loans, rawData, false);
    }

    public void printClients(List<DClient> clients) {
        clients.stream().sorted(Comparator.comparing(DClient::getEffectiveName)).forEach(c -> {
            c.refresh();
            Emeralds balance = c.getBalance(Instant.now());
            if (balance.amount() == 0) return;
            System.out.printf("%s %.2fLE%n", c.getEffectiveName(), balance.toLiquids());
        });
    }

    public void printLoans(List<DLoan> loans, ImportRawData rawData, boolean includeCorrect) {
        for (DLoan loan : loans) {
            if (loan.isActive()) continue;
            Emeralds owed = loan.getTotalOwed().add(loan.getTotalPaid());
            String client = loan.getClient().getEffectiveName() + " " + loan.getClient().getId();
            long id = loan.getId();
            RawLoan raw = rawData.getLoan(id);
            Emeralds finalPayment = raw.getFinalPayment();
            long diff = owed.amount() - finalPayment.amount();
            double percDiff = diff / (double) owed.amount();
            if (Math.abs(diff) < 64 || Math.abs(percDiff) < .05) {
                if (includeCorrect)
                    System.out.printf("%s paid correctly enough! (%d)%d%%%n", client, diff,
                        (int) (percDiff * 100));
            } else {
                // positive means paid too much
                double diffLiquids = Emeralds.of(diff).toLiquids();
                System.out.printf("difference (%.2fLE)%d%% %s, loan(%d), paid %.2fLE, real %.2fLE%n",
                    diffLiquids,
                    (int) (percDiff * 100),
                    client,
                    id,
                    finalPayment.toLiquids(),
                    owed.toLiquids());
            }
        }
    }

    private boolean shouldReset() {
        // todo
        return true;
    }

    private boolean shouldEnable() {
        // todo
        return true;
    }

    public boolean isProduction() {
        // todo
        return false;
    }

    @Override
    public String getName() {
        return "Import";
    }
}
