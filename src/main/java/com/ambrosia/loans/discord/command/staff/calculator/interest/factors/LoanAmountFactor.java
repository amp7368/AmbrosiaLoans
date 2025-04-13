package com.ambrosia.loans.discord.command.staff.calculator.interest.factors;

import static com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoice.wrap;

import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestFactor;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoice;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoiceData;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.util.function.Function;

public class LoanAmountFactor extends InterestFactor {

    @Override
    public String name() {
        return "Loan Amount";
    }

    @Override
    public String description(String username) {
        return "What is the initial amount of the loan?";
    }

    @Override
    protected String emoji() {
        return AmbrosiaEmoji.INVESTMENT_STAKE.toString();
    }

    @Override
    protected InterestMultiplierChoice[] choices() {
        return LoanAmountMultiplierChoice.values();
    }

    @Override
    protected Function<String, InterestMultiplierChoice> parser() {
        return LoanAmountMultiplierChoice::valueOf;
    }

    @Override
    public void setup(ActiveRequestLoan request) {
        Emeralds amount = request.getAmount();
        long threshold1 = Emeralds.stxToEmeralds(5).amount();
        long threshold2 = Emeralds.stxToEmeralds(10).amount();
        long threshold3 = Emeralds.stxToEmeralds(20).amount();
        long threshold4 = Emeralds.stxToEmeralds(30).amount();
        LoanAmountMultiplierChoice choice;
        if (amount.lt(threshold1)) {
            choice = LoanAmountMultiplierChoice.TINY_LOAN;
        } else if (amount.lt(threshold2)) {
            choice = LoanAmountMultiplierChoice.SMALL_LOAN;
        } else if (amount.lt(threshold3)) {
            choice = LoanAmountMultiplierChoice.NORMAL_LOAN;
        } else if (amount.lt(threshold4)) {
            choice = LoanAmountMultiplierChoice.LARGE_LOAN;
        } else
            choice = LoanAmountMultiplierChoice.VERY_LARGE_LOAN;
        setCalculatedChoice(choice);
    }

    private enum LoanAmountMultiplierChoice implements InterestMultiplierChoice {
        VERY_LARGE_LOAN(wrap("30+ stacks", 0.85)),
        LARGE_LOAN(wrap("20-29 stacks", 0.90)),
        NORMAL_LOAN(wrap("10-19 stacks", 0.95)),
        SMALL_LOAN(wrap("5-9 stacks", 1.00)),
        TINY_LOAN(wrap("< 5 stacks", 1.05));

        private final InterestMultiplierChoiceData data;

        LoanAmountMultiplierChoice(InterestMultiplierChoiceData data) {
            this.data = data;
        }

        @Override
        public InterestMultiplierChoiceData data() {
            return data;
        }
    }
}
