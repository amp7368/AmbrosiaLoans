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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public interface LoanAccess {

    default void changeToNewRate(double newRate, Instant startDate) {
        try (Transaction transaction = DB.beginTransaction()) {
            changeToNewRate(newRate, startDate, transaction);
            transaction.commit();
        }
    }

    default void changeToNewRate(double newRate, Instant startDate, Transaction transaction) {
        DLoan entity = getEntity();
        List<DLoanSection> oldSections = entity.getSections();
        List<DLoanSection> newSections = new ArrayList<>();
        boolean isPassedDate = false;
        for (int i = 0, size = oldSections.size(); i < size; i++) {
            DLoanSection section = oldSections.get(i);
            if (isPassedDate || section.isEndBeforeOrEq(startDate)) {
                // Either: add the new sections back
                // or keep old sections the same
                newSections.add(section);
            } else if (section.getStartDate().equals(startDate)) {
                section.setRate(newRate);
                newSections.add(section);
                isPassedDate = true;
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

        newSections.removeIf(section -> section.getEndDate() != null && section.getTotalDuration().isZero());
        fixDuplicateRates(newSections);

        oldSections.stream()
            .filter(Predicate.not(newSections::contains))
            .forEach(section -> section.delete(transaction));

        newSections.forEach(section -> section.save(transaction));
        entity.setSections(newSections);
        entity.checkIsFrozen(false);
        entity.save(transaction);
    }

    private void fixDuplicateRates(List<DLoanSection> sections) {
        sections.sort(Comparator.comparing(DLoanSection::getStartDate));
        DLoanSection lastSection = null;
        boolean changesMade = false;
        for (Iterator<DLoanSection> iterator = sections.iterator(); iterator.hasNext(); ) {
            DLoanSection section = iterator.next();
            if (lastSection != null && lastSection.getRate() == section.getRate()) {
                lastSection.setEndDate(section.getEndDate());
                iterator.remove();
                changesMade = true;
            }
            lastSection = section;
        }
        if (changesMade) fixDuplicateRates(sections);
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
        Instant end = Objects.requireNonNullElseGet(endDate, Instant::now);
        return getPayments().stream()
            .filter(pay -> !pay.getDate().isBefore(startDate))
            .filter(pay -> !pay.getDate().isAfter(end))
            .map(DLoanPayment::getAmount)
            .reduce(Emeralds.zero(), Emeralds::add);
    }

    List<DLoanPayment> getPayments();

    default double getCurrentRate() {
        return getEntity().getLastSection().getRate();
    }

    default boolean isFrozen() {
        return getCurrentRate() == 0 && isActive();
    }

    default boolean isActive() {
        return getEntity().getStatus().isActive();
    }

    default boolean isDefaulted() {
        return getEntity().getStatus() == DLoanStatus.DEFAULTED;
    }

    default boolean isPaid() {
        return getEntity().getStatus() == DLoanStatus.PAID;
    }

    default Emeralds getAccumulatedInterest() {
        DLoan loan = getEntity();
        return loan.getTotalOwed()
            .minus(loan.getInitialAmount())
            .add(loan.getTotalPaid());
    }

    default Double getRateAt(Instant effectiveDate) {
        return getEntity().getSections()
            .stream()
            .filter(s -> s.isDateDuring(effectiveDate))
            .findAny()
            .map(DLoanSection::getRate)
            .orElse(null);
    }
}
