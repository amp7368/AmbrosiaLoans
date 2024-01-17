package com.ambrosia.loans.database.account.event.loan.query;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.DLoanStatus;
import com.ambrosia.loans.database.account.event.loan.collateral.DCollateral;
import com.ambrosia.loans.database.account.event.loan.payment.DLoanPayment;
import com.ambrosia.loans.database.account.event.loan.section.DLoanSection;
import com.ambrosia.loans.database.base.ModelApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.util.CreateEntityException;
import com.ambrosia.loans.discord.request.cash.ActiveRequestLoan;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LoanApi extends ModelApi<DLoan> implements LoanAccess<LoanApi> {

    public LoanApi(DLoan entity) {
        super(entity);
    }

    public static LoanApi createLoan(DClient client, Emeralds amount, double rate, DStaffConductor conductor) {
        // todo allow different starting dates
        DLoan loan = new DLoan(client, amount.amount(), rate, conductor);
        System.out.println(conductor);
        loan.save();
        return api(loan);
    }

    public static LoanApi createLoan(ActiveRequestLoan request) throws CreateEntityException {
        DLoan loan = new DLoan(request);

        try (Transaction transaction = DB.beginTransaction()) {
            loan.save(transaction);
            for (String link : request.getCollateral())
                new DCollateral(loan, link).save(transaction);
            transaction.commit();
        }
        loan.refresh();
        return api(loan);
    }


    public static List<LoanApi> findClientLoans(DClient client) {
        return api(new QDLoan().where()
            .client.eq(client)
            .findStream());
    }

    public static List<LoanApi> findClientActiveLoans(DClient client) {
        return api(new QDLoan().where()
            .client.eq(client)
            .status.eq(DLoanStatus.ACTIVE)
            .findStream());
    }

    public static List<LoanApi> findAllActiveLoans() {
        return api(new QDLoan().where().status.eq(DLoanStatus.ACTIVE).findStream());
    }

    private static List<LoanApi> api(Stream<DLoan> stream) {
        return stream.map(LoanApi::api).toList();
    }

    public static LoanApi api(DLoan loan) {
        return new LoanApi(loan);
    }

    public static DLoanPayment makePayment(DLoan loan, Emeralds amount) {
        return makePayment(loan, amount, Instant.now());
    }

    public static DLoanPayment makePayment(DLoan loan, Emeralds amount, Instant timestamp) {
        long totalOwed = loan.getTotalOwed().amount();
        if (amount.amount() > totalOwed) {
            String msg = "Paying too much! Attempted to pay %s on %s loan{%s}".formatted(amount, loan.getTotalOwed(), loan.getId());
            throw new IllegalArgumentException(msg);
        }
        DLoanPayment payment = new DLoanPayment(loan, Timestamp.from(timestamp), amount.amount());
        try (Transaction transaction = DB.beginTransaction()) {
            loan.makePayment(payment);
            payment.save(transaction);
            loan.save(transaction);
            transaction.commit();
        }
        return payment;
    }

    public void freeze() {
        // todo
    }

    public boolean isAfter(Instant date) {
        return entity.getStartDate().isAfter(date);
    }

    @Override
    public LoanApi getSelf() {
        return this;
    }

    public void changeToNewRate(double newRate, Instant startDate) {
        List<DLoanSection> oldSections = getEntity().getSections();
        List<DLoanSection> newSections = new ArrayList<>();
        boolean isPassedDate = false;
        for (int i = 0, size = oldSections.size(); i < size; i++) {
            DLoanSection section = oldSections.get(i);
            if (isPassedDate || section.isEndBefore(startDate)) {
                // Either: add the new sections back
                // or keep old sections the same
                newSections.add(section);
            } else {
                section.setEndDate(startDate);
                newSections.add(section);
                DLoanSection addSection = new DLoanSection(getEntity(), newRate, startDate);
                newSections.add(addSection);
                if (i + 1 == size) break;
                addSection.setEndDate(oldSections.get(i + 1).getStartDate());
                isPassedDate = true;
            }
        }
        try (Transaction transaction = DB.beginTransaction()) {
            oldSections.stream()
                .filter(Predicate.not(newSections::contains))
                .forEach(section -> section.delete(transaction));

            newSections.forEach(section -> section.save(transaction));
            transaction.commit();
        }
        refresh();
    }
}
