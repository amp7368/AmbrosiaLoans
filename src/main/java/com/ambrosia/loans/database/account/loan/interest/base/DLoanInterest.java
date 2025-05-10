package com.ambrosia.loans.database.account.loan.interest.base;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.interest.legacy.DLegacyInterest;
import com.ambrosia.loans.database.account.loan.interest.standard.DStandardInterest;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = Id.NAME)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DStandardInterest.class, name = "standard"),
    @JsonSubTypes.Type(value = DLegacyInterest.class, name = "legacy")
})
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public abstract class DLoanInterest<Calculator extends LoanInterestCalculator<?, ?>> {

    protected DLoanInterest() {
    }

    public final InterestCheckpoint getInterest(InterestCheckpoint checkpoint, Instant end) {
        checkpoint.resetInterest();
        Calculator calculator = createCalculator(checkpoint, end);
        calculator.init();

        if (calculator.checkErrors()) return checkpoint;
        return calculator.getInterest();
    }

    public abstract Calculator createCalculator(InterestCheckpoint checkpoint, Instant end);

    @NotNull
    public abstract InterestCheckpoint createInitialCheckpoint(DLoan loan);
}
