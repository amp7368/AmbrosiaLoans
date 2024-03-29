package com.ambrosia.loans.discord.request.base;

import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;

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
        AmbrosiaEmoji prefix;
        if (error) prefix = AmbrosiaEmoji.CHECK_ERROR;
        else if (warning) prefix = AmbrosiaEmoji.CHECK_ERROR;
        else prefix = AmbrosiaEmoji.CHECK_SUCCESS;
        return "- " + prefix.spaced() + msg;
    }
}
