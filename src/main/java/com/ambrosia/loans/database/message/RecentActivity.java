package com.ambrosia.loans.database.message;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import java.time.Instant;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RecentActivity {

    @NotNull
    private final RecentActivityType type;
    @NotNull
    private final Instant date;
    @NotNull
    private final Function<RecentActivity, String> setMsg;
    @Nullable
    private String msg;
    @Nullable
    private DClientMessage lastReminded;
    @Nullable
    private Instant dateOrSystem;

    public RecentActivity(@NotNull RecentActivityType type, @NotNull Instant date, @NotNull Function<RecentActivity, String> msg) {
        this.type = type;
        this.date = date;
        this.setMsg = msg;
    }

    @NotNull
    public RecentActivityType getType() {
        return type;
    }

    @NotNull
    public Instant getDate() {
        return date;
    }

    public Instant getDateOrSystem() {
        if (dateOrSystem != null) return dateOrSystem;

        boolean isSystem = this.lastReminded != null && date.isBefore(lastReminded.getDateCreated());
        return this.dateOrSystem = isSystem ? lastReminded.getDateCreated() : date;
    }

    public String getDateStr() {
        return formatDate(date);
    }

    @NotNull
    public String getMsg() {
        if (this.msg != null) return msg;
        return this.msg = setMsg.apply(this);
    }

    @Override
    public String toString() {
        return this.getMsg();
    }

    public boolean isBefore(Instant otherDate) {
        return otherDate.isBefore(this.date);
    }

    public <T> boolean isBefore(T ifNull, Function<T, Instant> getOtherDate) {
        if (ifNull == null) return false;
        Instant otherDate = getOtherDate.apply(ifNull);
        return otherDate.isAfter(this.date);
    }

    public void addReminded(DClientMessage lastReminded) {
        this.lastReminded = lastReminded;
        this.dateOrSystem = null;
    }
}
