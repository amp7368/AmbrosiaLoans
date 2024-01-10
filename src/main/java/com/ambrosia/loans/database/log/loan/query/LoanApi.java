package com.ambrosia.loans.database.log.loan.query;

import com.ambrosia.loans.database.base.ModelApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.log.loan.DLoan;
import com.ambrosia.loans.database.log.loan.DLoanStatus;
import com.ambrosia.loans.database.log.loan.section.DLoanSection;
import com.ambrosia.loans.discord.base.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LoanApi extends ModelApi<DLoan> implements LoanAccess<LoanApi> {

    public LoanApi(DLoan entity) {
        super(entity);
    }

    public static LoanApi createLoan(DClient client, Emeralds amount, double rate, long brokerId) {
        // todo allow different starting dates
        DLoan loan = new DLoan(client.getAccountLog(), amount.amount(), rate, brokerId);
        loan.save();
        return api(loan);
    }

    public static List<LoanApi> findClientLoans(DClient client) {
        return api(new QDLoan().where().account.eq(client.getAccountLog()).findStream());
    }

    public static List<LoanApi> findClientActiveLoans(DClient client) {
        return api(new QDLoan().where().and().account.eq(client.getAccountLog()).status.eq(DLoanStatus.ACTIVE).endAnd().findStream());
    }

    public static List<LoanApi> findAllActiveLoans() {
        return api(new QDLoan().where().status.eq(DLoanStatus.ACTIVE).findStream());
    }

    private static List<LoanApi> api(Stream<DLoan> stream) {
        return stream.map(LoanApi::api).toList();
    }

    private static LoanApi api(DLoan loan) {
        return new LoanApi(loan);
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
            System.out.println(oldSections.size());
            System.out.println(newSections.size());

            oldSections.stream()
                .filter(Predicate.not(newSections::contains))
                .forEach(section -> section.delete(transaction));

            newSections.forEach(section -> section.save(transaction));
            transaction.commit();
        }
        refresh();
    }
}
