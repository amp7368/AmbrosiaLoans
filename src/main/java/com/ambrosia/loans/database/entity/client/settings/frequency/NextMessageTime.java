package com.ambrosia.loans.database.entity.client.settings.frequency;

import java.time.Instant;

public record NextMessageTime(Instant first, Instant next, String display) {

}
