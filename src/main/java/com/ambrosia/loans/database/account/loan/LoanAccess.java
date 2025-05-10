package com.ambrosia.loans.database.account.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.account.adjust.AdjustApi;
import com.ambrosia.loans.database.account.collateral.DCollateral;
import com.ambrosia.loans.database.account.collateral.DCollateralStatus;
import com.ambrosia.loans.database.account.loan.LoanApi.LoanAlterApi;
import com.ambrosia.loans.database.account.loan.section.DLoanSection;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterCreateApi;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.exception.InvalidStaffConductorException;
import com.ambrosia.loans.database.system.exception.OverpaymentException;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
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
import org.jetbrains.annotations.Nullable;

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

    default DLoanPayment makeMigrationPayment(Emeralds emeralds) {
        return makeMigrationPayment(emeralds, Instant.now());
    }

    default DLoanPayment makeMigrationPayment(Emeralds emeralds, Instant date) {
        if (emeralds.isNegative()) throw new IllegalArgumentException("Cannot make negative payment!");
        DLoan loan = getEntity();
        DLoanPayment payment = new DLoanPayment(loan, date, emeralds.amount(), DStaffConductor.SYSTEM);
        try (Transaction transaction = DB.beginTransaction()) {
            loan.makePayment(payment, transaction);
            transaction.commit();
        }
        return payment;
    }

    default DLoanPayment makePayment(ActiveRequestPayment request) throws InvalidStaffConductorException, OverpaymentException {
        return makePayment(request.getPayment(), request.getTimestamp(), request.getConductor());
    }

    default DLoanPayment makePayment(Emeralds emeralds, Instant timestamp, DStaffConductor staff) throws OverpaymentException {
        if (emeralds.isNegative()) throw new IllegalArgumentException("Cannot make negative payment!");

        DLoan loan = getEntity();
        Emeralds totalOwed = loan.getTotalOwed(timestamp);
        if (emeralds.gt(totalOwed.amount())) throw new OverpaymentException(emeralds, totalOwed);

        DLoanPayment payment = new DLoanPayment(loan, timestamp, emeralds.amount(), staff);
        try (Transaction transaction = DB.beginTransaction()) {
            loan.makePayment(payment, transaction);
            transaction.commit();
        }
        payment.refresh();
        loan.refresh();
        AlterCreateApi.create(staff, AlterCreateType.PAYMENT, payment.getId());
        loan.getClient().refresh();

        if (loan.isPaid()) {
            Instant adjustmentDate = payment.getDate().plusMillis(1);
            Emeralds adjustment = loan.getTotalOwed(adjustmentDate);
            Ambrosia.get().logger().info("Loan is paid on {}. Adjustment is {}.", formatDate(adjustmentDate), adjustment);
            if (!adjustment.isZero()) {
                AdjustApi.createAdjustment(staff, loan, adjustment, adjustmentDate);
            }
            for (DCollateral collateral : loan.getCollateral()) {
                if (collateral.getStatus() != DCollateralStatus.COLLECTED) continue;
                LoanAlterApi.markCollateral(staff, collateral, timestamp, DCollateralStatus.RETURNED, null);
            }
        }

        RunBankSimulation.simulateAsync(payment.getDate());
        return payment;
    }

    default Emeralds getTotalPaid() {
        DLoan loan = getEntity();
        return getTotalPaid(loan.getStartDate(), null);
    }

    default Emeralds getTotalPaid(Instant startDate, @Nullable Instant endDate) {
        Instant end = Objects.requireNonNullElseGet(endDate, Instant::now);
        return getEntity().getPayments().stream()
            .filter(pay -> pay.getDate().isAfter(startDate))
            .filter(pay -> !pay.getDate().isAfter(end))
            .map(DLoanPayment::getAmount)
            .reduce(Emeralds.zero(), Emeralds::add);
    }

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

    @Nullable
    default Double getRateAt(Instant effectiveDate) {
        DLoanSection section = getSectionAt(effectiveDate);
        return section == null ? null : section.getRate();
    }

    @Nullable
    default DLoanSection getSectionAt(Instant effectiveDate) {
        return getEntity().getSections()
            .stream()
            .filter(s -> s.isDateDuring(effectiveDate))
            .findAny()
            .orElse(null);
    }
}
