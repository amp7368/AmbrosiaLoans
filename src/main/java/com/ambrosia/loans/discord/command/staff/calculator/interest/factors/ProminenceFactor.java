package com.ambrosia.loans.discord.command.staff.calculator.interest.factors;

import static com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoice.wrap;

import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestFactor;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoice;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoiceData;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import java.util.function.Function;

public class ProminenceFactor extends InterestFactor {

    private static final String NAME = "Prominence";
    private static final String DESCRIPTION = "How well known of a player is %s?";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description(String username) {
        return DESCRIPTION.formatted(username);
    }

    @Override
    protected String emoji() {
        return AmbrosiaEmoji.CLIENT_ACCOUNT.toString();
    }

    @Override
    protected InterestMultiplierChoice[] choices() {
        return ProminenceMultiplierChoice.values();
    }

    @Override
    protected Function<String, InterestMultiplierChoice> parser() {
        return ProminenceMultiplierChoice::valueOf;
    }

    @Override
    public void setup(ActiveRequestLoan request) {
    }

    private enum ProminenceMultiplierChoice implements InterestMultiplierChoice {
        WELL_KNOWN(wrap("Well-known/Trusted", 0.8)),
        ESTABLISHED(wrap("Established", 0.9)),
        CASUAL(wrap("Casual", 1.0)),
        UNVERIFIED(wrap("Random/Unverified", 1.15));

        private final InterestMultiplierChoiceData data;

        ProminenceMultiplierChoice(InterestMultiplierChoiceData data) {
            this.data = data;
        }

        @Override
        public InterestMultiplierChoiceData data() {
            return data;
        }
    }
}
