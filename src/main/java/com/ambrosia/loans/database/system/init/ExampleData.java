package com.ambrosia.loans.database.system.init;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.ambrosia.loans.database.account.adjust.query.QDAdjustBalance;
import com.ambrosia.loans.database.account.adjust.query.QDAdjustLoan;
import com.ambrosia.loans.database.account.investment.DInvestment;
import com.ambrosia.loans.database.account.investment.InvestApi;
import com.ambrosia.loans.database.account.investment.query.QDInvestment;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.LoanApi.LoanCreateApi;
import com.ambrosia.loans.database.account.loan.collateral.query.QDCollateral;
import com.ambrosia.loans.database.account.loan.query.QDLoan;
import com.ambrosia.loans.database.account.loan.section.query.QDLoanSection;
import com.ambrosia.loans.database.account.payment.query.QDLoanPayment;
import com.ambrosia.loans.database.account.query.QDClientSnapshot;
import com.ambrosia.loans.database.account.withdrawal.WithdrawalApi;
import com.ambrosia.loans.database.account.withdrawal.query.QDWithdrawal;
import com.ambrosia.loans.database.alter.change.query.QDAlterChange;
import com.ambrosia.loans.database.alter.change.query.QDAlterChangeUndoHistory;
import com.ambrosia.loans.database.alter.create.query.QDAlterCreate;
import com.ambrosia.loans.database.alter.create.query.QDAlterCreateUndoHistory;
import com.ambrosia.loans.database.bank.query.QDBankSnapshot;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.entity.staff.query.QDStaffConductor;
import com.ambrosia.loans.database.message.query.QDCheckInMessage;
import com.ambrosia.loans.database.message.query.QDComment;
import com.ambrosia.loans.database.system.CreateEntityException;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Model;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ExampleData {

    private static DClient clientLoanA;
    private static DClient clientLoanB;
    private static DClient clientLoanC;
    private static DClient clientInvestA;
    private static DClient clientInvestB;
    private static DClient clientInvestC;
    private static DClient clientWithdrawalA;
    private static DClient clientNothingD;
    private static DClient clientManyLoans;

    public static void loadExample() {
        resetData();

        try {
            insertClients();
            insertLoans();
            insertInvestments();
            createPayments();
            withdrawals();
            clientManyLoans();
            RunBankSimulation.simulate(Instant.EPOCH);
        } catch (CreateEntityException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resetData() {
        new QDComment().delete();
        new QDLoanSection().delete();
        new QDLoanPayment().delete();
        new QDInvestment().delete();
        new QDWithdrawal().delete();
        new QDAdjustBalance().delete();
        new QDAdjustLoan().delete();
        new QDLoan().delete();
        new QDClientSnapshot().delete();
        new QDBankSnapshot().delete();
        new QDCollateral().delete();
        new QDCheckInMessage().delete();
        new QDAlterCreateUndoHistory().delete();
        new QDAlterChangeUndoHistory().delete();
        new QDAlterChange().delete();
        new QDAlterCreate().delete();
        new QDStaffConductor().delete();
        new QDClient().delete();
        InitDatabase.init();
    }

    private static void clientManyLoans() {
        Instant loan1Date = Instant.now().minus(30, DAYS);
        Instant loan2Date = Instant.now().minus(20, DAYS);
        Instant loan3Date = Instant.now().minus(10, DAYS);
        Instant loan4Date = Instant.now().minus(1, DAYS);
        Emeralds amount = Emeralds.leToEmeralds(128);
        DLoan loan1 = LoanCreateApi.createExampleLoan(clientManyLoans, amount, .05, DStaffConductor.SYSTEM, loan1Date);
        clientManyLoansMakePayment(loan1, loan1Date.plus(5, DAYS), false);
        clientManyLoansMakePayment(loan1, loan1Date.plus(6, DAYS), false);
        clientManyLoansMakePayment(loan1, loan1Date.plus(7, DAYS), true);
        DLoan loan2 = LoanCreateApi.createExampleLoan(clientManyLoans, amount, .06, DStaffConductor.SYSTEM, loan2Date);
        clientManyLoansMakePayment(loan2, loan2Date.plus(5, DAYS), false);
        clientManyLoansMakePayment(loan2, loan2Date.plus(8, DAYS), true);
        DLoan loan3 = LoanCreateApi.createExampleLoan(clientManyLoans, amount, .02, DStaffConductor.SYSTEM, loan3Date);
        clientManyLoansMakePayment(loan3, loan3Date.plus(3, DAYS), false);
        clientManyLoansMakePayment(loan3, loan3Date.plus(4, DAYS), true);
        DLoan loan4 = LoanCreateApi.createExampleLoan(clientManyLoans, amount, .03, DStaffConductor.SYSTEM, loan4Date);
        loan4.makePayment(Emeralds.leToEmeralds(16), loan4Date.plus(1, SECONDS));
    }

    private static void clientManyLoansMakePayment(DLoan loan, Instant paymentDate, boolean isAll) {
        RunBankSimulation.simulate(Instant.EPOCH);
        loan.getClient().refresh();
        Emeralds balance = loan.getClient().getBalance(paymentDate);
        long amount = isAll ? balance.amount() : balance.amount() / 2;
        loan.makePayment(Emeralds.of(amount).negative(), paymentDate);
    }


    private static void createPayments() {
        clients().forEach(Model::refresh);
        Instant now = Instant.now();
        clientLoanA.getLoans().get(0).makePayment(Emeralds.leToEmeralds(16), now.minus(5, DAYS));

        clientLoanB.getLoans().get(0).makePayment(Emeralds.leToEmeralds(63), now);
        clientLoanB.getLoans().get(0).makePayment(Emeralds.leToEmeralds(32), now.minus(10, DAYS));
        clientLoanB.getLoans().get(0).makePayment(Emeralds.leToEmeralds(32), now.minus(15, DAYS));
        clientLoanB.getLoans().get(0).makePayment(Emeralds.leToEmeralds(32), now.minus(20, DAYS));
        clientLoanB.getLoans().get(0).makePayment(Emeralds.leToEmeralds(32), now.minus(25, DAYS));

        clientLoanC.getLoans().get(0).makePayment(Emeralds.leToEmeralds(1));
    }

    private static List<DClient> clients() {
        return List.of(clientLoanA, clientLoanB, clientLoanC,
            clientInvestA, clientInvestB, clientInvestC,
            clientManyLoans, clientNothingD, clientWithdrawalA);
    }


    private static void insertLoans() throws CreateEntityException {
        DLoan loanA = LoanCreateApi.createExampleLoan(clientLoanA, Emeralds.leToEmeralds(64), .01, DStaffConductor.SYSTEM,
            Instant.now());
        Instant monthAgo = Instant.now().minus(Duration.ofDays(30));
        loanA.setStartDate(monthAgo.minus(Duration.ofDays(30)));
        loanA.save();
        loanA.changeToNewRate(.01, monthAgo);

        DLoan loanB = LoanCreateApi.createExampleLoan(clientLoanB, Emeralds.leToEmeralds(256), .00, DStaffConductor.SYSTEM,
            Instant.now());
        loanB.setStartDate(monthAgo.plus(1, DAYS));
        loanA.save();
        LoanCreateApi.createExampleLoan(clientLoanC, Emeralds.leToEmeralds(128), .01, DStaffConductor.SYSTEM, monthAgo);
    }

    private static void insertClients() {
        clientLoanA = new DClient("ClientLoanA");
        clientLoanB = new DClient("ClientLoanB");
        clientLoanC = new DClient("ClientLoanC");
        clientManyLoans = new DClient("ClientManyLoans");
        clientInvestA = new DClient("ClientInvestA");
        clientInvestB = new DClient("ClientInvestB");
        clientInvestC = new DClient("ClientInvestC");
        clientNothingD = new DClient("ClientNothingD");
        clientWithdrawalA = new DClient("ClientWithdrawal");
        clients().forEach(Model::save);
    }

    private static void withdrawals() {
        Instant bitAgo = Instant.now().minus(30, DAYS);
        WithdrawalApi.createMigrationWithdrawal(clientWithdrawalA, bitAgo, DStaffConductor.SYSTEM, Emeralds.leToEmeralds(19));
        WithdrawalApi.createMigrationWithdrawal(clientInvestB, bitAgo, DStaffConductor.SYSTEM, Emeralds.leToEmeralds(256));
    }

    private static void insertInvestments() {
        Instant longAgo = Instant.now().minus(60, DAYS);
        DInvestment investmentA = InvestApi.createMigrationInvestment(clientInvestA, longAgo, DStaffConductor.SYSTEM,
            Emeralds.leToEmeralds(100));
        DInvestment withdrawalA = InvestApi.createMigrationInvestment(clientWithdrawalA, longAgo, DStaffConductor.SYSTEM,
            Emeralds.leToEmeralds(19));
        for (int i = 0; i < 3; i++) {
            DInvestment investmentB = InvestApi.createMigrationInvestment(clientInvestB, longAgo.plus(i, MINUTES),
                DStaffConductor.SYSTEM,
                Emeralds.leToEmeralds(128));
        }
        DInvestment investmentC = InvestApi.createMigrationInvestment(clientInvestC, longAgo, DStaffConductor.SYSTEM,
            Emeralds.leToEmeralds(128));
    }

}
