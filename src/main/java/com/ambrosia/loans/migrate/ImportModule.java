package com.ambrosia.loans.migrate;

import apple.lib.modules.AppleModule;
import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.account.event.adjust.DAdjustBalance;
import com.ambrosia.loans.database.account.event.adjust.DAdjustLoan;
import com.ambrosia.loans.database.account.event.adjust.query.QDAdjustBalance;
import com.ambrosia.loans.database.account.event.adjust.query.QDAdjustLoan;
import com.ambrosia.loans.database.account.event.investment.DInvestment;
import com.ambrosia.loans.database.account.event.investment.query.QDInvestment;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.query.QDLoan;
import com.ambrosia.loans.database.account.event.withdrawal.DWithdrawal;
import com.ambrosia.loans.database.account.event.withdrawal.query.QDWithdrawal;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterCreateApi;
import com.ambrosia.loans.database.alter.gson.AlterCreateType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.init.ExampleData;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.discord.command.player.profile.page.ProfileTransactionsPage;
import com.ambrosia.loans.migrate.client.ImportedClient;
import com.ambrosia.loans.migrate.client.RawClient;
import com.ambrosia.loans.migrate.investment.ImportedInvestment;
import com.ambrosia.loans.migrate.investment.RawInvestment;
import com.ambrosia.loans.migrate.loan.ImportedLoan;
import com.ambrosia.loans.migrate.loan.RawLoan;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.io.File;
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
            .map(ImportedClient::toDB)
            .toList();
    }


    public static List<ImportedLoan> toDBLoans(ImportRawData rawData) {
        return rawData.loans().stream()
            .map(RawLoan::convert)
            .toList();
    }

    private static List<RawMakeAdjustment> toDBInvestments(ImportRawData rawData) {
        Comparator<RawInvestment> comparator = Comparator.comparing(RawInvestment::getClientId)
            .thenComparing(RawInvestment::date);
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
        List<RawMakeAdjustment> confirms = new ArrayList<>();
        imported.stream()
            .map(ImportedInvestment::confirmList)
            .forEach(confirms::addAll);
        confirms.sort(Comparator.comparing(RawMakeAdjustment::date));
        return confirms;
    }

    @Override
    public void onEnable() {
        if (!shouldEnable()) return;
        if (shouldReset()) ExampleData.resetData();

        ImportRawData rawData = ImportRawData.loadData();
        toDBClients(rawData);

        List<ImportedLoan> loans = toDBLoans(rawData);
        List<DLoan> loansDB = loans.stream().map(ImportedLoan::toDB).toList();

        List<RawMakeAdjustment> confirms = toDBInvestments(rawData);

        for (ImportedLoan loan : loans) {
            if (loan.getConfirm() != null)
                confirms.add(loan.getConfirm());
        }
        Instant lastDate = Instant.EPOCH;
        Instant lastLastDate = Instant.EPOCH;
        confirms.sort(Comparator.comparing(RawMakeAdjustment::date));
        for (int i = 0, size = confirms.size(); i < size; i++) {
            RawMakeAdjustment confirm = confirms.get(i);
            confirm.confirm(lastLastDate);
            lastLastDate = lastDate;
            lastDate = confirm.date();
            logger().info("confirm %d/%d %d %s".formatted(i + 1, size, confirm.getId(), confirm.date()));
        }
        logger().info("Running final simulation");
        RunBankSimulation.simulate(Instant.EPOCH);

        printLoans(loansDB, rawData, false);

        logger().info("Inserting DAlterCreates");
        insertCreationRecords();
        logger().info("Migration complete!");
    }

    private void insertCreationRecords() {
        for (DClient client : new QDClient().findList()) {
            AlterCreateApi.create(DStaffConductor.MIGRATION, AlterCreateType.CLIENT, client.getId());
        }
        for (DLoan loan : new QDLoan().findList()) {
            AlterCreateApi.create(DStaffConductor.MIGRATION, AlterCreateType.LOAN, loan.getId());
        }
        for (DAdjustLoan adjustment : new QDAdjustLoan().findList()) {
            AlterCreateApi.create(DStaffConductor.MIGRATION, AlterCreateType.ADJUST_LOAN, adjustment.getId());
        }
        for (DAdjustBalance adjustment : new QDAdjustBalance().findList()) {
            AlterCreateApi.create(DStaffConductor.MIGRATION, AlterCreateType.ADJUST_BALANCE, adjustment.getId());
        }
        for (DInvestment investment : new QDInvestment().findList()) {
            AlterCreateApi.create(DStaffConductor.MIGRATION, AlterCreateType.INVEST, investment.getId());
        }
        for (DWithdrawal withdrawal : new QDWithdrawal().findList()) {
            AlterCreateApi.create(DStaffConductor.MIGRATION, AlterCreateType.WITHDRAWAL, withdrawal.getId());
        }
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

    @Override
    public void onEnablePost() {
        Ambrosia.get().getFile("Graphs").mkdirs();
        logger().info("Creating graphs");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<Long> ids = new QDClient().findIds();
        for (Long clientId : ids) {
            DClient client = new QDClient()
                .id.eq(clientId)
                .fetch("accountSnapshots")
                .findOne();
            if (client == null || client.getAccountSnapshots().isEmpty()) continue;

            File file = Ambrosia.get().getFile("Graphs", "%d-%s.png".formatted(client.getId(), client.getEffectiveName()));
            ProfileTransactionsPage.createGraph(List.of(client), file);
        }
        logger().info("Completed making graphs!");
    }

    private boolean shouldReset() {
        return false;
    }

    private boolean shouldEnable() {
        // todo
        return true;
    }

    public boolean isProduction() {
        return Ambrosia.isProduction();
    }

    public boolean isQuick() {
        return false;
    }

    @Override
    public String getName() {
        return "Import";
    }
}
