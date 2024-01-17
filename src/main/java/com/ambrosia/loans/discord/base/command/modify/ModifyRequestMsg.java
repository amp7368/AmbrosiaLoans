package com.ambrosia.loans.discord.base.command.modify;

import com.ambrosia.loans.discord.system.theme.DiscordEmojis;

public record ModifyRequestMsg(String msg, boolean error, boolean warning) {

    public static ModifyRequestMsg error(String msg) {
        return new ModifyRequestMsg(msg, true, false);
    }

    public static ModifyRequestMsg warning(String msg) {
        return new ModifyRequestMsg(msg, false, true);
    }

    public static ModifyRequestMsg info(String msg) {
        return new ModifyRequestMsg(msg, false, false);
    }

    @Override
    public String toString() {
        String prefix;
        if (error) prefix = DiscordEmojis.DENY;
        else if (warning) prefix = DiscordEmojis.WARNING;
        else prefix = DiscordEmojis.SUCCESS;
        return prefix + " " + msg;
    }
}
