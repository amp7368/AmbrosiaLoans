package com.ambrosia.loans.discord.base.command.option;

import static com.ambrosia.loans.discord.DiscordModule.SIMPLE_DATE_FORMATTER;

import com.ambrosia.loans.discord.system.theme.AmbrosiaMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class CommandOptionDate extends CommandOptionMulti<String, Instant> {

    CommandOptionDate() {
        this("date", "Date (MM/DD/YY)");
    }

    CommandOptionDate(String name, String description) {
        super(name, description, OptionType.STRING, OptionMapping::getAsString,
            CommandOptionDate::parseDate);
    }

    private static Instant parseDate(String dateString) {
        try {
            TemporalAccessor date = SIMPLE_DATE_FORMATTER.parse(dateString);
            return Instant.from(date);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Override
    public Instant getRequired(SlashCommandInteractionEvent event) {
        AmbrosiaMessage errorMsg = ErrorMessages.dateParseError(getMap1(event), "MM/DD/YY");
        return super.getRequired(event, errorMsg);
    }
}
