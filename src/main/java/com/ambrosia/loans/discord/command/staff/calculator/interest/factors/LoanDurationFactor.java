package com.ambrosia.loans.discord.command.staff.calculator.interest.factors;

import static com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoice.wrap;

import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestFactor;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoice;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoiceData;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import java.util.function.Function;

public class LoanDurationFactor extends InterestFactor {

    @Override
    public String name() {
        return "Loan Duration";
    }

    @Override
    public String description(String username) {
        return "How long does staff expect the payback period will be?";
    }

    @Override
    protected String emoji() {
        return AmbrosiaEmoji.UNUSED_PAYMENT_REMINDER.toString();
    }

    @Override
    protected InterestMultiplierChoice[] choices() {
        return LoanDurationMultiplierChoice.values();
    }

    @Override
    protected Function<String, InterestMultiplierChoice> parser() {
        return LoanDurationMultiplierChoice::valueOf;
    }

    @Override
    protected void setup(ActiveRequestLoan request) {

    }

    private enum LoanDurationMultiplierChoice implements InterestMultiplierChoice {
        SHORT(wrap("1-2 weeks (Short)", 1.00)),
        NORMAL(wrap("3-4 weeks (Normal)", 1.10)),
        LONG(wrap("1+ months (Long)", 1.20));

        private final InterestMultiplierChoiceData data;

        LoanDurationMultiplierChoice(InterestMultiplierChoiceData data) {
            this.data = data;
        }

        @Override
        public InterestMultiplierChoiceData data() {
            return data;
        }
    }
}
