package com.ambrosia.loans.database.system.init;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.query.QDLoan;

public class InitLoanMeta {

    public static void verify() {
        new QDLoan().where().or()
            .meta.repayment.isNull()
            .meta.reason.isNull()
            .meta.discount.isNull()
            .findStream()
            .forEach(InitLoanMeta::verify);
    }

    private static void verify(DLoan loan) {
        loan.verifyMeta().save();
    }
}
