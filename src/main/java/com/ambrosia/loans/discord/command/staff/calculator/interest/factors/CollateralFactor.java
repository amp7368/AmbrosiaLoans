package com.ambrosia.loans.discord.command.staff.calculator.interest.factors;

import static com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoice.wrap;

import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestFactor;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoice;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoiceData;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.math.BigDecimal;
import java.util.function.Function;

public class CollateralFactor extends InterestFactor {

    private Emeralds loanAmount;

    @Override
    public String name() {
        return "Collateral";
    }

    @Override
    public String description(String username) {
        return "How valuable is the loan compared to collateral. (Collateral divided by %s)".formatted(loanAmount);
    }

    @Override
    protected String emoji() {
        return AmbrosiaEmoji.LOAN_COLLATERAL.toString();
    }

    @Override
    protected InterestMultiplierChoice[] choices() {
        return CollateralMultiplierChoice.values();
    }

    @Override
    protected Function<String, InterestMultiplierChoice> parser() {
        return CollateralMultiplierChoice::valueOf;
    }

    @Override
    protected void setup(ActiveRequestLoan request) {
        this.loanAmount = request.getAmount();
        addNote("150% of loan is " + factor(loanAmount, 1.50));
        addNote("125% of loan is " + factor(loanAmount, 1.25));
        addNote("115% of loan is " + factor(loanAmount, 1.15));
        addNote("100% of loan is " + factor(loanAmount, 1.00));
        addNote("90% of loan is " + factor(loanAmount, 0.90));
    }

    private Emeralds factor(Emeralds loanAmount, double multiplier) {
        long collateralValue = loanAmount.toBigDecimal()
            .multiply(BigDecimal.valueOf(multiplier))
            .longValue();

        long extra = collateralValue % 4096;
        long rounded = collateralValue - extra;
        return Emeralds.of(rounded);
    }

    private enum CollateralMultiplierChoice implements InterestMultiplierChoice {
        VERY_SECURE(wrap("150%+ of loan value (Very secure)", 0.7)),
        SECURE(wrap("125-150% of loan value (Secure)", 0.80)),
        LOW_RISK(wrap("115-124% of loan value (Low risk)", 0.90)),
        MODERATE_RISK(wrap("100-115% of loan value (Moderate risk)", 1.05)),
        HIGH_RISK(wrap("90-100% of loan value (High risk)", 1.15)),
        EXTREME_RISK(wrap("< 90% of loan value (Extreme risk)", 1.30));

        private final InterestMultiplierChoiceData data;

        CollateralMultiplierChoice(InterestMultiplierChoiceData data) {
            this.data = data;
        }

        @Override
        public InterestMultiplierChoiceData data() {
            return data;
        }
    }
}
