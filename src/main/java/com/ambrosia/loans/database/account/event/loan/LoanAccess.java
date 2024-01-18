package com.ambrosia.loans.database.account.event.loan;

import com.ambrosia.loans.database.account.event.loan.payment.DLoanPayment;
import com.ambrosia.loans.database.account.event.loan.section.DLoanSection;
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
    }

    default void freeze() {
        // todo
    }

    DLoan getEntity();

    default void makePayment(Emeralds emeralds) {
        DLoan entity = getEntity();
        DLoanPayment payment = new DLoanPayment(entity, Instant.now(), emeralds.amount());
        entity.makePayment(payment);
        try (Transaction transaction = DB.beginTransaction()) {
            payment.save(transaction);
            entity.save(transaction);
        }
    }
}
