package com.ambrosia.loans.database.loan.query;

import com.ambrosia.loans.database.base.ModelApi;
import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.database.loan.DLoan;
import com.ambrosia.loans.database.loan.DLoanStatus;
import com.ambrosia.loans.database.loan.collateral.DCollateral;
import com.ambrosia.loans.database.loan.section.DLoanSection;
import io.ebean.DB;
import io.ebean.Transaction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class LoanApi extends ModelApi<DLoan> implements LoanAccess<LoanApi> {

    public LoanApi(DLoan entity) {
        super(entity);
    }

    public static LoanApi createLoan(DClient client, List<DCollateral> collateral, int amount, double rate, long brokerId) {
        DLoan loan = new DLoan(client, amount, collateral, brokerId);
        try (Transaction transaction = DB.beginTransaction()) {
            loan.save(transaction);
            DLoanSection section = new DLoanSection(loan, rate, loan.getStartDate());
            section.save(transaction);
            transaction.commit();
            loan.addSection(section);
        }
        return api(loan);
    }

    public static List<LoanApi> findClientLoans(DClient client) {
        return api(new QDLoan().where().client.eq(client).findStream());
    }

    public static List<LoanApi> findClientActiveLoans(DClient client) {
        return api(new QDLoan().where().and().client.eq(client).status.eq(DLoanStatus.ACTIVE).endAnd().findStream());
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

    public void changeToNewRate(double rate, Instant startDate) {
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
                DLoanSection addSection = new DLoanSection(getEntity(), rate, startDate);
                newSections.add(addSection);
                if (i + 1 == size) break;
                addSection.setEndDate(oldSections.get(i + 1).getStartDate());
                isPassedDate = true;
            }
        }
        try (Transaction transaction = DB.beginTransaction()) {
            getEntity().getSections().forEach(section -> section.delete(transaction));
            getEntity().setSections(newSections);
            getEntity().save(transaction);
            transaction.commit();
        }
    }
}
