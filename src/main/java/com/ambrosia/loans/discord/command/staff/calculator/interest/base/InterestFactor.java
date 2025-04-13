package com.ambrosia.loans.discord.command.staff.calculator.interest.base;

import com.ambrosia.loans.discord.command.staff.calculator.interest.gui.InterestCalculatorGui;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.Builder;
import org.jetbrains.annotations.Nullable;

public abstract class InterestFactor {

    public static final String INTEREST_SELECT_ID = "multiplier";
    private final List<String> notes = new ArrayList<>();
    @Nullable
    private InterestMultiplierChoice currentChoice;
    @Nullable
    private InterestMultiplierChoice calculatedChoice;

    protected void addNote(String note) {
        notes.add(note);
    }

    protected InterestFactor setCalculatedChoice(InterestMultiplierChoice calculatedChoice) {
        this.calculatedChoice = calculatedChoice;
        this.currentChoice = calculatedChoice;
        return this;
    }

    public String getNotes() {
        return notes.stream()
            .map(s -> "- " + s)
            .collect(Collectors.joining("\n"));
    }

    public abstract String name();

    public abstract String description(String username);

    protected abstract String emoji();

    protected abstract InterestMultiplierChoice[] choices();

    protected abstract Function<String, InterestMultiplierChoice> parser();

    public void init(ActiveRequestLoan request) {
        setup(request);
        applyColors();
    }

    private void applyColors() {
        List<InterestMultiplierChoice> items = Arrays.stream(choices())
            .sorted(Comparator.comparingDouble(InterestMultiplierChoice::multiplier).reversed())
            .toList();
        List<AmbrosiaEmoji> colors = AmbrosiaEmoji.statusInOrder();

        int itemCount = items.size();
        int colorCount = colors.size();

        if (itemCount == 0 || colorCount == 0) return; // Edge case: Empty lists

        for (int i = 0; i < itemCount; i++) {
            double percent = (double) i / (itemCount - 1); // Percentage progress through items
            int colorIndex = (int) Math.round(percent * (colorCount - 1)); // Map to colors

            items.get(i).setColor(colors.get(colorIndex)); // Assign color
        }
    }

    protected abstract void setup(ActiveRequestLoan request);

    public void setChoice(String choice) {
        currentChoice = parser().apply(choice);
    }

    @Nullable
    public InterestMultiplierChoice getCurrentChoice() {
        return currentChoice;
    }

    public StringSelectMenu selectChoice() {
        List<SelectOption> options = Arrays.stream(choices())
            .map(InterestMultiplierChoice::toSelectOption)
            .toList();

        Builder select = StringSelectMenu.create(INTEREST_SELECT_ID)
            .setPlaceholder(name())
            .addOptions(options)
            .setRequiredRange(1, 1);
        if (currentChoice != null) select.setDefaultValues(currentChoice.name());

        return select.build();
    }

    public double getCurrentMult() {
        if (currentChoice == null) return 1;
        return currentChoice.multiplier();
    }

    public String getCurrentString() {
        if (currentChoice == null) {
            return "- %s (**%s1.00**) %s **%s**\n  - Not Set".formatted(
                AmbrosiaEmoji.CHECK_ERROR, InterestCalculatorGui.MULT_SYMBOL,
                emoji(), name()
            );
        }
        String label = currentChoice.label();
        double multiplier = currentChoice.multiplier();
        return "- %s (**%s%.2f**) %s **%s**\n  - %s".formatted(
            currentChoice.color(), InterestCalculatorGui.MULT_SYMBOL, multiplier,
            emoji(), name(),
            label);
    }

    public String getCalculatedDefaultString() {
        if (calculatedChoice == null) return "";
        String label = calculatedChoice.label();
        double multiplier = calculatedChoice.multiplier();
        return "- [Default] %s (**%s%.2f**) %s **%s**\n  - %s".formatted(
            calculatedChoice.color(), InterestCalculatorGui.MULT_SYMBOL, multiplier,
            emoji(), name(),
            label);
    }
}
