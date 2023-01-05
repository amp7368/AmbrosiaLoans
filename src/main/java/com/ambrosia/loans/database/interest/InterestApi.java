package com.ambrosia.loans.database.interest;

import com.ambrosia.loans.database.base.ModelApi;
import com.ambrosia.loans.database.interest.query.QDInterest;
import com.ambrosia.loans.database.loan.DLoan;
import com.ambrosia.loans.database.loan.LoanApi;

import java.sql.Timestamp;
import java.time.Instant;

public class InterestApi extends ModelApi<DInterest> {

    public InterestApi(DInterest entity) {
        super(entity);
    }

    public static InterestApi lastInterest(DLoan loan) {
        return api(new QDInterest().where().loan.eq(loan).orderBy().actionDate.desc().setMaxRows(1).findOne());
    }

    private static InterestApi api(DInterest entity) {
        return new InterestApi(entity);
    }

    public static InterestApi createInterest(LoanApi loan, Instant now) {
        DInterest interest = new DInterest(loan.entity, Timestamp.from(now));
        InterestApi api = api(interest);
        api.save();
        return api;
    }

    public boolean isBefore(Instant date) {
        return entity.actionDate.toInstant().isBefore(date);
    }
}
