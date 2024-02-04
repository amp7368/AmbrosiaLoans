package com.ambrosia.loans.database.account.event.loan;

import com.ambrosia.loans.database.account.event.loan.section.DLoanSection;
import com.ambrosia.loans.database.account.event.payment.DLoanPayment;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.request.payment.ActiveRequestPayment;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface LoanAccess {

    default void changeToNewRate(double newRate, Instant startDate) {
        DLoan entity = getEntity();
        List<DLoanSection> oldSections = entity.getSections();
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
                DLoanSection addSection = new DLoanSection(entity, newRate, startDate);
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
        entity.refresh();
        entity.checkIsFrozen(true);
    }

    DLoan getEntity();

    default DLoanPayment makePayment(Emeralds emeralds) {
        return makePayment(emeralds, Instant.now());
    }

    default DLoanPayment makePayment(Emeralds emeralds, Instant date) {
        if (emeralds.isNegative()) throw new IllegalArgumentException("Cannot make negative payment!");
        DLoan loan = getEntity();
        DLoanPayment payment = new DLoanPayment(loan, date, emeralds.amount(), DStaffConductor.SYSTEM);
        try (Transaction transaction = DB.beginTransaction()) {
            loan.makePayment(payment, transaction);
            transaction.commit();
        }
        return payment;
    }

    default DLoanPayment makePayment(ActiveRequestPayment request) throws InvalidStaffConductorException {
        Emeralds emeralds = request.getPayment();
        if (emeralds.isNegative()) throw new IllegalArgumentException("Cannot make negative payment!");
        DLoan loan = getEntity();
        DLoanPayment payment = new DLoanPayment(loan, request.getTimestamp(), emeralds.amount(), request.getConductor());
        try (Transaction transaction = DB.beginTransaction()) {
            loan.makePayment(payment, transaction);
            transaction.commit();
        }
        payment.refresh();
        loan.refresh();
        loan.getClient().refresh();
        RunBankSimulation.simulate(payment.getDate());
        return payment;
    }

    default Emeralds getTotalPaid() {
        DLoan loan = getEntity();
        return getTotalPaid(loan.getStartDate(), loan.getEndDate());
    }

    default Emeralds getTotalPaid(Instant startDate, Instant endDate) {
        return getPayments().stream()
            .filter(pay -> !pay.getDate().isBefore(startDate))
            .filter(pay -> !pay.getDate().isAfter(endDate))
            .map(DLoanPayment::getAmount)
            .reduce(Emeralds.zero(), Emeralds::add);
    }

    List<DLoanPayment> getPayments();

    default double getCurrentRate() {
        return getEntity().getLastSection().getRate();
    }

    default boolean isFrozen() {
        return getCurrentRate() == 0;
    }

    default boolean isActive() {
        return getEntity().getStatus().isActive();
    }

    default boolean isDefaulted() {
        return getEntity().getStatus() == DLoanStatus.DEFAULTED;
    }
}
