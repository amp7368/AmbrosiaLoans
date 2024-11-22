package com.ambrosia.loans.database.account.loan;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.exception.InvalidStaffConductorException;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public interface LoanBuilder {

    DClient getClient();

    Emeralds getAmount();

    DStaffConductor getConductor() throws InvalidStaffConductorException;

    String getReason();

    String getRepayment();

    @Nullable
    DClient getVouchClient();

    Instant getStartDate();

    @Nullable
    Double getRate();

    @Nullable
    String getDiscount();

    default Long getLoanId() {
        return null;
    }

    default Instant getStartDateOrNow() {
        return Objects.requireNonNullElseGet(getStartDate(), Instant::now);
    }
}
