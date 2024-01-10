package com.ambrosia.loans.database;

import com.ambrosia.loans.database.base.util.CreateEntityException;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.entity.staff.query.QDStaffConductor;
import com.ambrosia.loans.database.log.invest.DInvest;
import com.ambrosia.loans.database.log.invest.InvestApi;
import com.ambrosia.loans.database.log.invest.query.QDInvest;
import com.ambrosia.loans.database.log.loan.query.LoanApi;
import com.ambrosia.loans.database.log.loan.query.QDLoan;
import com.ambrosia.loans.discord.base.emerald.Emeralds;
import com.ambrosia.loans.discord.base.emerald.EmeraldsFormatter;
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

    public static void loadExample() {
        new QDStaffConductor().delete();
        new QDClient().delete();
        new QDLoan().delete();
        new QDInvest().delete();

        try {
            insertClients();
            insertLoans();
            insertInvestments();
        } catch (CreateEntityException e) {
            throw new RuntimeException(e);
        }
    }


    private static void insertLoans() throws CreateEntityException {
        LoanApi loanA = LoanApi.createLoan(clientLoanA, Emeralds.leToEmeralds(64), .05, 0);
        Instant monthAgo = Instant.now().minus(Duration.ofDays(30));
        loanA.getEntity().setStartDate(monthAgo.minus(Duration.ofDays(30)));
        loanA.getEntity().save();
        print(loanA);
        loanA.changeToNewRate(.33, monthAgo);

        print(loanA);

        LoanApi loanB = LoanApi.createLoan(clientLoanB, Emeralds.leToEmeralds(64), .04, 0);

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
        List.of(clientLoanA, clientLoanB, clientLoanC, clientInvestA, clientInvestB, clientInvestC).forEach(client -> {
            client.save();
            client.getAccountLog().save();
            client.getAccountSimulation().save();
        });
    }

    private static void insertInvestments() {
        DInvest investmentA = InvestApi.createInvestment(clientInvestA, DStaffConductor.SYSTEM, Emeralds.leToEmeralds(64));
        System.out.println(investmentA);
    }
}
