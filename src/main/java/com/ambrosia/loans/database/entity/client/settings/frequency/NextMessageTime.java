package com.ambrosia.loans.database.entity.client.settings.frequency;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import java.time.Instant;

public record NextMessageTime(Instant first, Instant next, String display) {

    public String message() {
        return "The next reminder will be in **%s** on *%s*".formatted(display, formatDate(next));
    }
}
