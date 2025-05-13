package com.ambrosia.loans.database.account.loan.interest.legacy;

import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.account.adjust.DAdjustLoan;
import com.ambrosia.loans.database.account.loan.interest.base.InterestCheckpoint;
import com.ambrosia.loans.database.account.loan.interest.base.LoanInterestCalculator;
import com.ambrosia.loans.database.account.loan.section.DLoanSection;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import java.time.Duration;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public class DLegacyInterestCalculator extends LoanInterestCalculator<DLegacyInterest, InterestCheckpoint> {

    public DLegacyInterestCalculator(DLegacyInterest settings, DLegacyInterestCheckpoint checkpoint, Instant end) {
        super(settings, checkpoint, end);
    }

    @Override
    protected boolean filterPayment(DLoanPayment payment) {
        Instant date = payment.getDate();
        Instant start = loan.getStartDate();
        return !date.isBefore(start) &&
            !date.isAfter(end);
    }

    @Override
    protected boolean filterAdjustment(DAdjustLoan adjustment) {
        Instant date = adjustment.getDate();
        Instant start = loan.getStartDate();
        return !date.isBefore(start) &&
            !date.isAfter(end);
    }

    @NotNull
    @Override
    public InterestCheckpoint getInterest() {
        DLegacyInterestCheckpoint checkpoint = new DLegacyInterestCheckpoint(loan);

        for (DLoanSection section : sections) {
            Instant estimatedCheckpoint = section.getEarliestOfEnd(end);
            Duration duration = Bank.legacySimpleWeeksDuration(Duration.between(checkpoint.lastUpdated(), estimatedCheckpoint));
            Instant nextCheckpoint = checkpoint.lastUpdated().plus(duration);
            section.accumulateInterest(checkpoint, nextCheckpoint);
        }

        long paymentsDelta = payments.stream()
            .mapToLong(payment -> payment.getAmount().amount())
            .sum();
        long adjustmentsDelta = adjustments.stream()
            .mapToLong(adjustment -> adjustment.getAmount().amount())
            .sum();

        long totalDelta = paymentsDelta + adjustmentsDelta;
        long negativeDelta = -totalDelta;
        checkpoint.updateBalance(negativeDelta, end);
        return checkpoint;
    }
}
