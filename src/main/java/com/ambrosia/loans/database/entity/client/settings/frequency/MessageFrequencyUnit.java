package com.ambrosia.loans.database.entity.client.settings.frequency;

import apple.utilities.util.Pretty;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import org.jetbrains.annotations.Nullable;

public enum MessageFrequencyUnit {
    DAILY(ChronoUnit.DAYS),
    WEEKLY(ChronoUnit.WEEKS),
    MONTHLY(ChronoUnit.MONTHS),
    NEVER(ChronoUnit.FOREVER),
    DEFAULT(null);

    private final ChronoUnit unit;

    MessageFrequencyUnit(ChronoUnit unit) {
        this.unit = unit;
    }

    @Nullable
    public TemporalUnit getUnit() {
        return unit;
    }

    public String display(int count, String ifDefault) {
        return switch (this) {
            case DAILY -> Pretty.plural(count, "%d Day".formatted(count));
            case WEEKLY -> Pretty.plural(count, "%d Week".formatted(count));
            case MONTHLY -> Pretty.plural(count, "%d Month".formatted(count));
            case NEVER -> "Never";
            case DEFAULT -> {
                if (ifDefault == null) throw new IllegalArgumentException("Default duration used DEFAULT units");
                yield "%s [Default]".formatted(ifDefault);
            }
        };
    }
}
