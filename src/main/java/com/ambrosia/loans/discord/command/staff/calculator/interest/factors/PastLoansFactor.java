package com.ambrosia.loans.discord.command.staff.calculator.interest.factors;

import static com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoice.wrap;
import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestFactor;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoice;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoiceData;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class PastLoansFactor extends InterestFactor {

    private static final String NAME = "Loan History";
    private static final String DESCRIPTION = "How much history does %s have with Ambrosia?";

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
        return AmbrosiaEmoji.LOAN_INTEREST.toString();
    }

    @Override
    protected InterestMultiplierChoice[] choices() {
        return PastLoansMultiplierChoice.values();
    }

    @Override
    protected Function<String, InterestMultiplierChoice> parser() {
        return PastLoansMultiplierChoice::valueOf;
    }

    @Override
    public void setup(ActiveRequestLoan request) {
        List<DLoan> loans = request.getClient()
            .getLoans().stream()
            .sorted(Comparator.comparing(DLoan::getStartDate).reversed())
            .toList();

        int payments = 0;
        int paidLoans = 0;
        for (DLoan loan : loans) {
            if (!loan.isPaid()) continue;
            paidLoans++;
            payments += loan.getPayments().size();
        }
        addNote("Made %d payments over %d paid loans".formatted(payments, paidLoans));

        for (DLoan loan : loans) {
            Emeralds amount = loan.getInitialAmount();
            String startDate = formatDate(loan.getStartDate(), true);
            if (!loan.isPaid()) {
                addNote("%s Active Loan from %s".formatted(amount, startDate));
                continue;
            }
            String endDate = formatDate(loan.getEndDate());
            addNote("%s Loan from %s to %s".formatted(amount, startDate, endDate));
        }
        PastLoansMultiplierChoice calculated = switch (paidLoans) {
            case 0 -> PastLoansMultiplierChoice.NO_HISTORY;
            case 1 -> PastLoansMultiplierChoice.SOME_HISTORY;
            case 2, 3 -> PastLoansMultiplierChoice.MODERATE_HISTORY;
            default -> PastLoansMultiplierChoice.LOTS_OF_HISTORY;
        };
        setCalculatedChoice(calculated);
    }

    private enum PastLoansMultiplierChoice implements InterestMultiplierChoice {
        LOTS_OF_HISTORY(wrap("Lots of History (4+ loans)", 0.75)),
        MODERATE_HISTORY(wrap("Moderate History (2-3 loans)", 0.85)),
        SOME_HISTORY(wrap("Some History (1 loan)", 1.0)),
        NO_HISTORY(wrap("Random/Unverified (0 loans)", 1.15));

        private final InterestMultiplierChoiceData data;

        PastLoansMultiplierChoice(InterestMultiplierChoiceData data) {
            this.data = data;
        }

        @Override
        public InterestMultiplierChoiceData data() {
            return data;
        }
    }
}
