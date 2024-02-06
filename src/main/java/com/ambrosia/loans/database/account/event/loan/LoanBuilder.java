package com.ambrosia.loans.database.account.event.loan;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
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

    Long getLoanId();
}
