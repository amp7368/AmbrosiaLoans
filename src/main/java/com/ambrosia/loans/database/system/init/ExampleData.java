package com.ambrosia.loans.database.system.init;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.ambrosia.loans.database.account.balance.query.QDAccountSnapshot;
import com.ambrosia.loans.database.account.event.invest.DInvest;
import com.ambrosia.loans.database.account.event.invest.InvestApi;
import com.ambrosia.loans.database.account.event.invest.query.QDInvest;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.LoanApi.LoanCreateApi;
import com.ambrosia.loans.database.account.event.loan.collateral.query.QDCollateral;
import com.ambrosia.loans.database.account.event.loan.payment.query.QDLoanPayment;
import com.ambrosia.loans.database.account.event.loan.query.QDLoan;
import com.ambrosia.loans.database.account.event.loan.section.query.QDLoanSection;
import com.ambrosia.loans.database.bank.query.QDBankSnapshot;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.messages.checkin.query.QDCheckInMessage;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.database.util.CreateEntityException;
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
        new QDLoanSection().delete();
        new QDLoanPayment().delete();
        new QDLoan().delete();
        new QDInvest().delete();
        new QDAccountSnapshot().delete();
        new QDBankSnapshot().delete();
        new QDCollateral().delete();
        new QDCheckInMessage().delete();
        new QDClient().delete();

        try {
            insertClients();
            insertLoans();
            insertInvestments();
            createPayments();
            withdrawals();
            clientManyLoans();
            RunBankSimulation.simulateFromDate(Instant.EPOCH);
        } catch (CreateEntityException e) {
            throw new RuntimeException(e);
        }
    }

    private static void clientManyLoans() {
        Instant loan1Date = Instant.now().minus(30, DAYS);
        Instant loan2Date = Instant.now().minus(20, DAYS);
        Instant loan3Date = Instant.now().minus(10, DAYS);
        Instant loan4Date = Instant.now().minus(1, DAYS);
        Emeralds amount = Emeralds.leToEmeralds(128);
        DLoan loan1 = LoanCreateApi.createLoan(clientManyLoans, amount, .05, DStaffConductor.SYSTEM, loan1Date);
        clientManyLoansMakePayment(loan1, loan1Date.plus(5, DAYS), false);
        clientManyLoansMakePayment(loan1, loan1Date.plus(6, DAYS), false);
        clientManyLoansMakePayment(loan1, loan1Date.plus(7, DAYS), true);
        DLoan loan2 = LoanCreateApi.createLoan(clientManyLoans, amount, .06, DStaffConductor.SYSTEM, loan2Date);
        clientManyLoansMakePayment(loan2, loan2Date.plus(5, DAYS), false);
        clientManyLoansMakePayment(loan2, loan2Date.plus(8, DAYS), true);
        DLoan loan3 = LoanCreateApi.createLoan(clientManyLoans, amount, .02, DStaffConductor.SYSTEM, loan3Date);
        clientManyLoansMakePayment(loan3, loan3Date.plus(3, DAYS), false);
        clientManyLoansMakePayment(loan3, loan3Date.plus(4, DAYS), true);
        DLoan loan4 = LoanCreateApi.createLoan(clientManyLoans, amount, .03, DStaffConductor.SYSTEM, loan4Date);
        loan4.makePayment(Emeralds.leToEmeralds(16), loan4Date.plus(1, SECONDS));
    }

    private static void clientManyLoansMakePayment(DLoan loan, Instant paymentDate, boolean isAll) {
        RunBankSimulation.simulateFromDate(Instant.EPOCH);
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
        DLoan loanA = LoanCreateApi.createLoan(clientLoanA, Emeralds.leToEmeralds(64), .01, DStaffConductor.SYSTEM, Instant.now());
        Instant monthAgo = Instant.now().minus(Duration.ofDays(30));
        loanA.setStartDate(monthAgo.minus(Duration.ofDays(30)));
        loanA.save();
        loanA.changeToNewRate(.01, monthAgo);

        DLoan loanB = LoanCreateApi.createLoan(clientLoanB, Emeralds.leToEmeralds(256), .00, DStaffConductor.SYSTEM, Instant.now());
        loanB.setStartDate(monthAgo.plus(1, DAYS));
        loanA.save();
        LoanCreateApi.createLoan(clientLoanC, Emeralds.leToEmeralds(128), .01, DStaffConductor.SYSTEM, monthAgo);
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
        clientManyLoans.setDiscord(ClientDiscordDetails.fromManual(253646208084475904L,
            "https://cdn.discordapp.com/avatars/253646208084475904/65b6d3079a00a363788e031f92e41f18.png",
            "appleptr16"));
        clientInvestA.setDiscord(ClientDiscordDetails.fromManual(
            283000305380229121L,
            null,
            "Tealy"
        ));
        clients().forEach(Model::save);
    }

    private static void withdrawals() {
        Instant bitAgo = Instant.now().minus(30, DAYS);
        InvestApi.createWithdrawal(clientWithdrawalA, bitAgo, DStaffConductor.SYSTEM, Emeralds.leToEmeralds(19));
        InvestApi.createWithdrawal(clientInvestB, bitAgo, DStaffConductor.SYSTEM, Emeralds.leToEmeralds(256));
    }

    private static void insertInvestments() {
        Instant longAgo = Instant.now().minus(60, DAYS);
        DInvest investmentA = InvestApi.createInvestment(clientInvestA, longAgo, DStaffConductor.SYSTEM,
            Emeralds.leToEmeralds(100));
        DInvest withdrawalA = InvestApi.createInvestment(clientWithdrawalA, longAgo, DStaffConductor.SYSTEM,
            Emeralds.leToEmeralds(19));
        for (int i = 0; i < 3; i++) {
            DInvest investmentB = InvestApi.createInvestment(clientInvestB, longAgo.plus(i, MINUTES), DStaffConductor.SYSTEM,
                Emeralds.leToEmeralds(128));
        }
        DInvest investmentC = InvestApi.createInvestment(clientInvestC, longAgo, DStaffConductor.SYSTEM,
            Emeralds.leToEmeralds(128));
    }
}
