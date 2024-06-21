package com.ambrosia.loans.discord.base.command.option;

import static com.ambrosia.loans.discord.DiscordModule.SIMPLE_DATE_FORMATTER;

import com.ambrosia.loans.discord.system.theme.AmbrosiaMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.Nullable;

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
            TemporalAccessor parsed = SIMPLE_DATE_FORMATTER.parse(dateString);
            Instant date = Instant.from(parsed);
            if (ChronoUnit.DAYS.between(date, Instant.now()) == 0)
                return Instant.now();
            return date;
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Override
    public AmbrosiaMessage getErrorMessage(CommandInteraction event) {
        String dateString = getMap1(event);
        if (dateString == null) return super.getErrorMessage(event);
        return ErrorMessages.dateParseError(dateString, "MM/DD/YY");
    }

    @Nullable
    public Instant getOrParseError(CommandInteraction event, Instant fallback) {
        Instant val = getOptional(event, fallback);
        if (val == null)
            getErrorMessage(event).replyError(event);
        return val;
    }
}
