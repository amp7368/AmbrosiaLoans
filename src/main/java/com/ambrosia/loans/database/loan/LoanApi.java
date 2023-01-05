package com.ambrosia.loans.database.loan;

import com.ambrosia.loans.database.base.ModelApi;
import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.database.collateral.DCollateral;
import com.ambrosia.loans.database.loan.query.QDLoan;
import com.ambrosia.loans.database.util.CreateEntityException;
import com.ambrosia.loans.database.util.UniqueMessages;
import io.ebean.DB;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

public class LoanApi extends ModelApi<DLoan> implements LoanAccess<LoanApi> {

    public LoanApi(DLoan entity) {
        super(entity);
    }

    public static LoanApi createLoan(DClient client, List<DCollateral> collateral, int amount, double rate, long brokerId)
            throws CreateEntityException {
        DLoan loan = new DLoan(client, collateral, amount, rate, brokerId);
        UniqueMessages.saveIfUnique(loan);
        loan = DB.find(loan.getClass(), loan.getId());
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
        return this.getStartDate().toInstant().isAfter(date);
    }

    @Override
    public LoanApi getSelf() {
        return this;
    }
}
