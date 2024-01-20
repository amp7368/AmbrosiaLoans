package com.ambrosia.loans.database.system.init;

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
import com.ambrosia.loans.util.emerald.EmeraldsFormatter;
import io.ebean.Model;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
            RunBankSimulation.simulateFromDate(Instant.EPOCH);
            createPayments();
            RunBankSimulation.simulateFromDate(Instant.EPOCH);
            withdrawals();
            RunBankSimulation.simulateFromDate(Instant.EPOCH);
        } catch (CreateEntityException e) {
            throw new RuntimeException(e);
        }
    }


    private static void createPayments() {
        clients().forEach(Model::refresh);
        Instant now = Instant.now();
        clientLoanA.getLoans().get(0).makePayment(Emeralds.leToEmeralds(16), now.minus(5, ChronoUnit.DAYS));

        clientLoanB.getLoans().get(0).makePayment(Emeralds.leToEmeralds(32), now.minus(10, ChronoUnit.DAYS));
        clientLoanB.getLoans().get(0).makePayment(Emeralds.leToEmeralds(32), now.minus(15, ChronoUnit.DAYS));
        clientLoanB.getLoans().get(0).makePayment(Emeralds.leToEmeralds(32), now.minus(20, ChronoUnit.DAYS));
        clientLoanB.getLoans().get(0).makePayment(Emeralds.leToEmeralds(32), now.minus(25, ChronoUnit.DAYS));

        clientLoanC.getLoans().get(0).makePayment(Emeralds.leToEmeralds(1));
        print(clientLoanA.getLoans().get(0));
        print(clientLoanB.getLoans().get(0));
    }

    private static List<DClient> clients() {
        return List.of(clientLoanA, clientLoanB, clientLoanC,
            clientInvestA, clientInvestB, clientInvestC,
            clientNothingD,
            clientWithdrawalA);
    }


    private static void insertLoans() throws CreateEntityException {
        DLoan loanA = LoanCreateApi.createLoan(clientLoanA, Emeralds.leToEmeralds(64), .01, DStaffConductor.SYSTEM);
        Instant monthAgo = Instant.now().minus(Duration.ofDays(30));
        loanA.setStartDate(monthAgo.minus(Duration.ofDays(30)));
        loanA.save();
        loanA.changeToNewRate(.01, monthAgo);

        DLoan loanB = LoanCreateApi.createLoan(clientLoanB, Emeralds.leToEmeralds(256), .00, DStaffConductor.SYSTEM);
        loanB.setStartDate(monthAgo.plus(1, ChronoUnit.DAYS));
        loanA.save();
        DLoan loanC = LoanCreateApi.createLoan(clientLoanC, Emeralds.leToEmeralds(128), .01, DStaffConductor.SYSTEM);
    }

    private static void print(DLoan loanA) {
        System.err.println(EmeraldsFormatter.of().setBold(false).format(loanA.getTotalOwed()));
    }

    private static void insertClients() {
        clientLoanA = new DClient("ClientLoanA");
        clientLoanB = new DClient("ClientLoanB");
        clientLoanC = new DClient("ClientLoanC");
        clientInvestA = new DClient("ClientInvestA");
        clientInvestA.setDiscord(ClientDiscordDetails.fromManual(
            283000305380229121L,
            null,
            "Tealy"
        ));
        clientInvestB = new DClient("ClientInvestB");
        clientInvestB.setDiscord(ClientDiscordDetails.fromManual(253646208084475904L,
            "https://cdn.discordapp.com/avatars/253646208084475904/65b6d3079a00a363788e031f92e41f18.png",
            "appleptr16"));
        clientInvestC = new DClient("ClientInvestC");
        clientNothingD = new DClient("ClientNothingD");
        clientWithdrawalA = new DClient("ClientWithdrawal");
        clients().forEach(Model::save);
    }

    private static void withdrawals() {
        Instant bitAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        InvestApi.createWithdrawal(clientWithdrawalA, bitAgo, DStaffConductor.SYSTEM, Emeralds.leToEmeralds(19));
        InvestApi.createWithdrawal(clientInvestB, bitAgo, DStaffConductor.SYSTEM, Emeralds.leToEmeralds(256));
    }

    private static void insertInvestments() {
        Instant longAgo = Instant.now().minus(60, ChronoUnit.DAYS);
        DInvest investmentA = InvestApi.createInvestment(clientInvestA, longAgo, DStaffConductor.SYSTEM,
            Emeralds.leToEmeralds(100));
        DInvest withdrawalA = InvestApi.createInvestment(clientWithdrawalA, longAgo, DStaffConductor.SYSTEM,
            Emeralds.leToEmeralds(19));
        for (int i = 0; i < 3; i++) {
            DInvest investmentB = InvestApi.createInvestment(clientInvestB, longAgo, DStaffConductor.SYSTEM,
                Emeralds.leToEmeralds(128));
        }
        DInvest investmentC = InvestApi.createInvestment(clientInvestC, longAgo, DStaffConductor.SYSTEM,
            Emeralds.leToEmeralds(128));
    }
}
