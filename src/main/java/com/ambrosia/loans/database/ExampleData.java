package com.ambrosia.loans.database;

import com.ambrosia.loans.database.base.util.CreateEntityException;
import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.database.loan.query.LoanApi;
import com.ambrosia.loans.discord.base.Emeralds;
import io.ebean.DB;

import java.sql.Timestamp;
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
        try {
            insertClients();
            insertLoans();
        } catch (CreateEntityException e) {
            throw new RuntimeException(e);
        }
    }

    private static void insertLoans() throws CreateEntityException {
        LoanApi loanA = LoanApi.createLoan(clientLoanA, List.of(), Emeralds.leToEmeralds(64), .05, 0);
        Instant monthAgo = Instant.now().minus(Duration.ofDays(30));
        loanA.getEntity().setStartDate(Timestamp.from(monthAgo.minus(Duration.ofDays(30))));
        loanA.getEntity().save();
        loanA.changeToNewRate(33.3, monthAgo);

        LoanApi loanB = LoanApi.createLoan(clientLoanB, List.of(), Emeralds.leToEmeralds(64), .04, 0);

        LoanApi loanC = LoanApi.createLoan(clientLoanC, List.of(), Emeralds.leToEmeralds(128), .01, 0);
    }

    private static void insertClients() {
        clientLoanA = new DClient("ClientLoanA");
        clientLoanB = new DClient("ClientLoanB");
        clientLoanC = new DClient("ClientLoanC");
        clientInvestA = new DClient("ClientInvestA");
        clientInvestB = new DClient("ClientInvestB");
        clientInvestC = new DClient("ClientInvestC");
        DB.insertAll(List.of(clientLoanA, clientLoanB, clientLoanC, clientInvestA, clientInvestB, clientInvestC));
    }
}
