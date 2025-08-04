package com.ambrosia.loans.database.system.init;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.interest.legacy.DLegacyInterest;
import com.ambrosia.loans.database.account.loan.interest.standard.DStandardInterest;
import com.ambrosia.loans.database.account.loan.query.QDLoan;
import com.ambrosia.loans.database.version.ApiVersionList.ApiVersionListLoan;
import java.util.List;

public class InitLoanInterest {

    public static void verify() {
        List<DLoan> loans = new QDLoan().where()
            .interest.isNull()
            .version.eq(ApiVersionListLoan.SIMPLE_INTEREST_EXACT.getDB())
            .findList();
        for (DLoan loan : loans) {
            loan.setInterestMeta(new DStandardInterest());
            loan.save();
        }
        loans = new QDLoan().where()
            .interest.isNull()
            .version.eq(ApiVersionListLoan.SIMPLE_INTEREST_WEEKLY.getDB())
            .findList();
        for (DLoan loan : loans) {
            loan.setInterestMeta(new DLegacyInterest());
            loan.save();
        }
    }
}
