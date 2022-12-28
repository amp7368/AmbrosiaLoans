package com.ambrosia.loans.database.loan;

import com.ambrosia.loans.database.base.ModelApi;
import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.database.collateral.DCollateral;
import com.ambrosia.loans.database.util.CreateEntityException;
import com.ambrosia.loans.database.util.UniqueMessages;
import java.util.List;

public class LoanApi extends ModelApi<DLoan> {

    public LoanApi(DLoan loan) {
        super(loan);
    }

    public static LoanApi createLoan(DClient client, List<DCollateral> collateral, int amount, double rate, long brokerId)
        throws CreateEntityException {
        DLoan loan = new DLoan(client, collateral, amount, rate, brokerId);
        UniqueMessages.saveIfUnique(loan);
        return api(loan);
    }

    private static LoanApi api(DLoan loan) {
        return new LoanApi(loan);
    }
}
