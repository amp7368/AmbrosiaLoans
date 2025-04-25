package com.ambrosia.loans.discord.command.staff.calculator.interest.gui;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.gui.ClientGui;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestFactor;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestMultiplierChoice;
import com.ambrosia.loans.discord.command.staff.calculator.interest.factors.CollateralFactor;
import com.ambrosia.loans.discord.command.staff.calculator.interest.factors.CreditWorthinessFactor;
import com.ambrosia.loans.discord.command.staff.calculator.interest.factors.LoanAmountFactor;
import com.ambrosia.loans.discord.command.staff.calculator.interest.factors.LoanDurationFactor;
import com.ambrosia.loans.discord.command.staff.calculator.interest.factors.PastLoansFactor;
import com.ambrosia.loans.discord.command.staff.calculator.interest.factors.PlaytimeFactor;
import com.ambrosia.loans.discord.command.staff.calculator.interest.factors.ProminenceFactor;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import discord.util.dcf.DCF;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class InterestCalculatorGui extends ClientGui {

    public static final String MULT_SYMBOL = "\u00D7";
    private static final String SPACED_MULT = " %s ".formatted(MULT_SYMBOL);
    private static final double BASE_RATE = 3;
    private final ActiveRequestLoan request;
    private final DClient client;

    private final List<InterestFactor> factors;

    public InterestCalculatorGui(ActiveRequestLoan request, DCF dcf, DCFEditMessage reply) {
        super(request.getClient(), dcf, reply);
        this.request = request;
        this.client = request.getClient();
        this.factors = setFactors();
        addGuiPages();
    }


    private @NotNull List<InterestFactor> setFactors() {
        List<InterestFactor> factors = List.of(
            new ProminenceFactor(),
            new PastLoansFactor(),
            new PlaytimeFactor(),
            new LoanAmountFactor(),
            new CollateralFactor(),
            new LoanDurationFactor(),
            new CreditWorthinessFactor()
        );
        factors.forEach(factor -> factor.init(request));
        return factors;
    }

    private void addGuiPages() {
        for (InterestFactor factor : factors) {
            new InterestMultiplierPage(this, factor).addPageToGui();
        }
    }

    public DClient getClient() {
        return client;
    }

    public String getClientName() {
        return client.getEffectiveName();
    }

    public boolean isCompleted() {
        return factors.stream()
            .map(InterestFactor::getCurrentChoice)
            .noneMatch(Objects::isNull);
    }

    public double getFinalRate() {
        double rate = BASE_RATE * getFinalMultiplier();
        return Math.round(rate * 100) / 100d;
    }

    public double getFinalMultiplier() {
        return factors.stream()
            .map(InterestFactor::getCurrentChoice)
            .filter(Objects::nonNull)
            .mapToDouble(InterestMultiplierChoice::multiplier)
            .reduce(1, (a, b) -> a * b);
    }

    public String getCalculationString() {
        StringBuilder displayFactors = new StringBuilder();
        StringBuilder formula = new StringBuilder();
        StringBuilder calculation = new StringBuilder();

        formula.append("Interest Rate = %.2f%%".formatted(BASE_RATE));
        calculation.append("Interest Rate = %.2f%%".formatted(BASE_RATE));
        for (InterestFactor factor : factors) {
            String factorName = factor.name();

            formula.append(SPACED_MULT).append(factorName);
            calculation.append(SPACED_MULT).append("%.2f".formatted(factor.getCurrentMult()));
            displayFactors.append(factor.getCurrentString()).append("\n");
        }

        double finalMultiplier = getFinalMultiplier();
        String calculationPartial = "Interest Rate = %.2f%%".formatted(BASE_RATE) +
            SPACED_MULT + " %.2f%%".formatted(finalMultiplier);
        return """
            __**Values**__
            %s
            __**Calculation**__
            %s
            %s
            ### Final Interest Rate = %.2f%%
            """.formatted(displayFactors, calculation, calculationPartial, getFinalRate());
    }
}
