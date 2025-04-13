package com.ambrosia.loans.discord.command.staff.calculator.interest.factors;

import static com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoice.wrap;

import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestFactor;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoice;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoiceData;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import java.util.function.Function;

public class CreditWorthinessFactor extends InterestFactor {

    @Override
    public String name() {
        return "Creditworthiness ";
    }

    @Override
    public String description(String username) {
        return "What is %s's creditworthiness?".formatted(username);
    }

    @Override
    protected String emoji() {
        return AmbrosiaEmoji.LOAN_DISCOUNT.toString();
    }

    @Override
    protected InterestMultiplierChoice[] choices() {
        return CreditWorthinessMultiplierChoice.values();
    }

    @Override
    protected Function<String, InterestMultiplierChoice> parser() {
        return CreditWorthinessMultiplierChoice::valueOf;
    }

    @Override
    protected void setup(ActiveRequestLoan request) {
    }

    private enum CreditWorthinessMultiplierChoice implements InterestMultiplierChoice {
        HIGH_TRUST(wrap("High Trust (Well-known and reliable)", 0.75)),
        MEDIUM_TRUST(wrap("Medium Trust (Some history, no issues)", 0.85)),
        LOW_TRUST(wrap("Low Trust (Unreliable or unknown)", 1.10)),
        NO_TRUST(wrap("No Trust (Past bad behavior)", 1.30));

        private final InterestMultiplierChoiceData data;

        CreditWorthinessMultiplierChoice(InterestMultiplierChoiceData data) {
            this.data = data;
        }

        @Override
        public InterestMultiplierChoiceData data() {
            return data;
        }

    }
}
