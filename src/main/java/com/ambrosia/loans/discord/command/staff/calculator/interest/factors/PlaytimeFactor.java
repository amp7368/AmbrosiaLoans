package com.ambrosia.loans.discord.command.staff.calculator.interest.factors;

import static com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoice.wrap;

import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestFactor;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoice;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoiceData;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import java.util.function.Function;

public class PlaytimeFactor extends InterestFactor {

    @Override
    public String name() {
        return "Playtime";
    }

    @Override
    public String description(String username) {
        return "How consistently active is %s?".formatted(username);
    }

    @Override
    protected String emoji() {
        return AmbrosiaEmoji.UNUSED_SORT.toString();
    }

    @Override
    protected InterestMultiplierChoice[] choices() {
        return PlaytimeMultiplierChoice.values();
    }

    @Override
    protected Function<String, InterestMultiplierChoice> parser() {
        return PlaytimeMultiplierChoice::valueOf;
    }

    @Override
    public void setup(ActiveRequestLoan request) {

    }

    private enum PlaytimeMultiplierChoice implements InterestMultiplierChoice {
        LOTS_OF_HISTORY(wrap("Consistently active for months", 0.85)),
        MODERATE_HISTORY(wrap("Regular player, but had short breaks", 0.95)),
        SOME_HISTORY(wrap("Recently returned after a long break", 1.10)),
        NO_HISTORY(wrap("Unclear or low activity", 1.20));

        private final InterestMultiplierChoiceData data;

        PlaytimeMultiplierChoice(InterestMultiplierChoiceData data) {
            this.data = data;
        }

        @Override
        public InterestMultiplierChoiceData data() {
            return data;
        }
    }
}
