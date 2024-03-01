package com.ambrosia.loans.discord.check.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.discord.check.base.CheckDate;
import com.ambrosia.loans.discord.command.staff.alter.AlterCommandField;
import java.time.Instant;
import java.util.Objects;

public class CheckLoanEnd extends CheckDate {

    private final AlterCommandField<DLoan> loan;
    private final AlterCommandField<Instant> startDate;

    public CheckLoanEnd(AlterCommandField<DLoan> loan,
        AlterCommandField<Instant> startDate) {
        this.loan = loan;
        this.startDate = startDate;
    }

    @Override
    public void checkAll(Instant value, CheckErrorList error) {
        super.checkAll(value, error);
        DLoan loan = this.loan.get();
        if (loan == null) return;
        Instant start = Objects.requireNonNullElseGet(startDate.get(), loan::getStartDate);
        if (value.isBefore(start)) {
            String msg = "Start of %s is before end of %s!".formatted(formatDate(start), formatDate(value));
            error.addError(msg);
        }
    }
}
