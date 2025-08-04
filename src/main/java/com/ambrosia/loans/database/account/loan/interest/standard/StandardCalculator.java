package com.ambrosia.loans.database.account.loan.interest.standard;

import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.account.loan.interest.base.InterestCheckpoint;
import com.ambrosia.loans.database.account.loan.interest.base.LoanInterestCalculator;
import com.ambrosia.loans.database.account.loan.section.DLoanSection;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public class StandardCalculator extends LoanInterestCalculator<DStandardInterest, DStandardInterestCheckpoint> {

    public StandardCalculator(DStandardInterest settings, DStandardInterestCheckpoint checkpoint, Instant end) {
        super(settings, checkpoint, end);
    }

    @NotNull
    @Override
    public InterestCheckpoint getInterest() {
        // while there's payments, make payments until there's none left
        // each iteration, increment either sectionIndex or paymentIndex
        while (payments.hasNext() || adjustments.hasNext()) {
            if (!sections.hasNext()) break; // payments in future means running simulation
            nextStep();

            DLoanSection section = sections.get();
            if (sections.isNextStep()) {
                Instant date = section.getEarliestOfEnd(end);
                accumulateSectionInterest(section, date);
                sections.increment();
            } else {
                boolean isDateAfter = nextStepDate.isAfter(end);
                if (isDateAfter) break;
                // make balance change
                accumulateSectionInterest(section, nextStepDate);
                checkpoint.updateBalance(nextStepDelta.amount(), nextStepDate);
            }
        }
        while (sections.hasNext()) {
            accumulateSectionInterest(sections.get());
            sections.increment();
        }
        return checkpoint;
    }

    private void accumulateSectionInterest(DLoanSection section) {
        accumulateSectionInterest(section, section.getEarliestOfEnd(this.end));
    }

    protected void accumulateSectionInterest(DLoanSection section, Instant accumulateUntil) {
        BigDecimal sectionInterest = getInterest(section, accumulateUntil);
        checkpoint.accumulateInterest(sectionInterest, accumulateUntil);
    }

    private BigDecimal getInterest(DLoanSection section, Instant accumulateUntil) {
        Duration duration = section.getDuration(checkpoint.lastUpdated(), accumulateUntil);
        if (!duration.isPositive()) return BigDecimal.ZERO;

        BigDecimal rate = BigDecimal.valueOf(section.getRate());
        BigDecimal principal = BigDecimal.valueOf(checkpoint.getPrincipal());
        return Bank.interest(duration, principal, rate);
    }
}
