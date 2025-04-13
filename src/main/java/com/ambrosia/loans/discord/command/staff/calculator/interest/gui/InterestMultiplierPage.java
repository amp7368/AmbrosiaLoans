package com.ambrosia.loans.discord.command.staff.calculator.interest.gui;

import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.command.staff.calculator.interest.base.InterestFactor;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import discord.util.dcf.gui.base.page.DCFGuiPage;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class InterestMultiplierPage extends DCFGuiPage<InterestCalculatorGui> implements SendMessage {

    private final InterestFactor factor;

    public InterestMultiplierPage(InterestCalculatorGui parent, InterestFactor factor) {
        super(parent);
        this.factor = factor;
        registerSelectString(InterestFactor.INTEREST_SELECT_ID, this::onSelect);
    }

    private void onSelect(StringSelectInteractionEvent event) {
        List<SelectOption> selectedOptions = event.getSelectedOptions();
        if (selectedOptions.size() != 1) return;
        factor.setChoice(selectedOptions.get(0).getValue());
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder().setColor(AmbrosiaColor.GREEN);
        ClientMessage.of(parent.getClient()).clientAuthor(embed);

        addHeader(embed);

        embed.appendDescription(factor.getCurrentString());
        embed.appendDescription("\n");
        String calculatedDefault = factor.getCalculatedDefaultString();
        if (!calculatedDefault.isBlank()) {
            embed.appendDescription("%s".formatted(calculatedDefault));
            embed.appendDescription("\n");
        }
        addNotes(embed);
        embed.appendDescription("\n\n");
        embed.appendDescription(parent.getCalculationString());

        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .addActionRow(factor.selectChoice())
            .addActionRow(btnPrev(), btnNext(), btnCalculate())
            .build();

    }

    private void addNotes(EmbedBuilder embed) {
        String notes = factor.getNotes();
        if (!notes.isBlank()) {
            embed.appendDescription("__**Notes**__\n");
            embed.appendDescription(notes);
            embed.appendDescription("\n");
        }
    }

    public void addHeader(EmbedBuilder embed) {
        String factorName = factor.name();
        String factorTitle = title(factorName, getPageNum(), getPageSize() - 1);
        embed.appendDescription("# %s\n".formatted(factorTitle));

        String description = factor.description(parent.getClientName());
        embed.appendDescription("%s\n".formatted(description));
    }

    public Button btnCalculate() {
        return btnLast().withLabel("To Calculation Page");
    }
}
