package com.ambrosia.loans.database.loan;

import com.ambrosia.loans.database.base.BaseAccess;
import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.database.collateral.DCollateral;

import java.sql.Timestamp;
import java.util.List;

public interface LoanAccess<Self> extends BaseAccess<Self, DLoan> {
    default int getId() {
        return getEntity().id;
    }

    default DClient getClient() {
        return getEntity().client;
    }

    default List<DCollateral> getCollateral() {
        return getEntity().collateral;
    }

    default int getAmount() {
        return getEntity().amount;
    }

    default double getRate() {
        return getEntity().rate;
    }

    default Timestamp getStartDate() {
        return getEntity().startDate;
    }

    default Timestamp getEndDate() {
        return getEntity().endDate;
    }

    default DLoanStatus getStatus() {
        return getEntity().status;
    }

    default long getBrokerId() {
        return getEntity().brokerId;
    }

    default LoanMoment getMoment() {
        return getEntity().moment;
    }

}
