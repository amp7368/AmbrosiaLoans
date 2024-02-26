package com.ambrosia.loans.discord.check.loan;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.discord.check.CheckError;
import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.discord.command.staff.alter.AlterCommandField;
import java.time.Instant;
import java.util.Optional;

public class CheckLoanDefaulted extends CheckError<Boolean> {

    private final AlterCommandField<DLoan> loan;
    private final AlterCommandField<Instant> endDate;

    public CheckLoanDefaulted(AlterCommandField<DLoan> loan, AlterCommandField<Instant> endDate) {
        this.loan = loan;
        this.endDate = endDate;
    }

    @Override
    public void checkAll(Boolean value, CheckErrorList error) {
        if (!value) return;

        DLoan loan = this.loan.get();
        if (loan == null) return;
        Instant endDate = Optional.ofNullable(this.endDate.get())
            .orElse(loan.getEndDate());
        if (loan.isPaid() || loan.isDefaulted()) {
            String status = loan.isPaid() ? "paid" : "defaulted";
            error.addError("Loan has already been %s! Cannot mark as defaulted.".formatted(status));
            return;
        }
        if (endDate != null)
            error.addError("Loan already has an end date somehow?");

    }
}
