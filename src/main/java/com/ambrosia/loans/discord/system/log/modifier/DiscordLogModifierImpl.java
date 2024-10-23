package com.ambrosia.loans.discord.system.log.modifier;

import com.ambrosia.loans.discord.system.log.DiscordLog;

public record DiscordLogModifierImpl(int priority, DiscordLogModifier base) implements DiscordLogModifier {

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void modify(DiscordLog log) {
        base.modify(log);
    }
}
