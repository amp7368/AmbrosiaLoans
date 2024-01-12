package com.ambrosia.loans.database;

import com.ambrosia.loans.database.bank.RunBankSimulation;
import com.ambrosia.loans.database.bank.query.QDBankSnapshot;
import com.ambrosia.loans.database.base.util.CreateEntityException;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.messages.checkin.query.QDCheckInMessage;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.entity.staff.query.QDStaffConductor;
import com.ambrosia.loans.database.log.invest.DInvest;
import com.ambrosia.loans.database.log.invest.InvestApi;
import com.ambrosia.loans.database.log.invest.query.QDInvest;
import com.ambrosia.loans.database.log.loan.collateral.query.QDCollateral;
import com.ambrosia.loans.database.log.loan.payment.query.QDLoanPayment;
import com.ambrosia.loans.database.log.loan.query.LoanApi;
import com.ambrosia.loans.database.log.loan.query.QDLoan;
import com.ambrosia.loans.database.log.loan.section.query.QDLoanSection;
import com.ambrosia.loans.database.simulate.snapshot.query.QDAccountSnapshot;
import com.ambrosia.loans.discord.base.emerald.Emeralds;
import com.ambrosia.loans.discord.base.emerald.EmeraldsFormatter;
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
    private static DClient clientNothingD;

    public static void loadExample() {
        new QDStaffConductor().delete();
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
            RunBankSimulation.simulateFromDate(Instant.EPOCH);
        } catch (CreateEntityException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createPayments() {
        clients().forEach(Model::refresh);
        LoanApi.makePayment(clientLoanA.getLoans().get(0), Emeralds.leToEmeralds(16));
        LoanApi.makePayment(clientLoanB.getLoans().get(0), Emeralds.leToEmeralds(32));
        LoanApi.makePayment(clientLoanB.getLoans().get(0), Emeralds.leToEmeralds(32));
        print(clientLoanA.getLoans().get(0).api());
        print(clientLoanB.getLoans().get(0).api());
    }

    private static List<DClient> clients() {
        return List.of(clientLoanA, clientLoanB, clientLoanC, clientInvestA, clientInvestB, clientInvestC, clientNothingD);
    }


    private static void insertLoans() throws CreateEntityException {
        LoanApi loanA = LoanApi.createLoan(clientLoanA, Emeralds.leToEmeralds(64), .05, 0);
        Instant monthAgo = Instant.now().minus(Duration.ofDays(30));
        loanA.getEntity().setStartDate(monthAgo.minus(Duration.ofDays(30)));
        loanA.getEntity().save();
        loanA.changeToNewRate(.01, monthAgo);

        LoanApi loanB = LoanApi.createLoan(clientLoanB, Emeralds.leToEmeralds(64), .00, 0);

        LoanApi loanC = LoanApi.createLoan(clientLoanC, Emeralds.leToEmeralds(128), .01, 0);
    }

    private static void print(LoanApi loanA) {
        System.err.println(EmeraldsFormatter.of().setBold(false).format(loanA.getEntity().getTotalOwed()));
    }

    private static void insertClients() {
        clientLoanA = new DClient("ClientLoanA");
        clientLoanB = new DClient("ClientLoanB");
        clientLoanC = new DClient("ClientLoanC");
        clientInvestA = new DClient("ClientInvestA");
        clientInvestB = new DClient("ClientInvestB");
        clientInvestC = new DClient("ClientInvestC");
        clientNothingD = new DClient("ClientNothingD");
        List.of(clientLoanA, clientLoanB, clientLoanC, clientInvestA, clientInvestB, clientInvestC, clientNothingD).forEach(
            Model::save);
    }

    private static void insertInvestments() {
        DInvest investmentA = InvestApi.createInvestment(clientInvestA, DStaffConductor.SYSTEM, Emeralds.leToEmeralds(5));
        for (int i = 0; i < 6; i++) {
            DInvest investmentB = InvestApi.createInvestment(clientInvestB, DStaffConductor.SYSTEM, Emeralds.leToEmeralds(64));
        }
        DInvest investmentC = InvestApi.createInvestment(clientInvestC, DStaffConductor.SYSTEM, Emeralds.leToEmeralds(128));
    }
}
