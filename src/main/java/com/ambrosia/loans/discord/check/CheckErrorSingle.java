package com.ambrosia.loans.discord.check;

import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;

public record CheckErrorSingle(String msg, CheckErrorLevel level) {

    public static CheckErrorSingle fatal(String msg) {
        return new CheckErrorSingle(msg, CheckErrorLevel.FATAL);
    }

    public static CheckErrorSingle error(String msg) {
        return new CheckErrorSingle(msg, CheckErrorLevel.ERROR);
    }

    public static CheckErrorSingle warning(String msg) {
        return new CheckErrorSingle(msg, CheckErrorLevel.WARNING);
    }

    public static CheckErrorSingle info(String msg) {
        return new CheckErrorSingle(msg, CheckErrorLevel.INFO);
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean includeEmoji) {
        if (!includeEmoji) return msg;

        AmbrosiaEmoji prefix = switch (level) {
            case FATAL, ERROR, WARNING -> AmbrosiaEmoji.CHECK_ERROR;
            case INFO -> AmbrosiaEmoji.CHECK_SUCCESS;
        };
        return "- " + prefix.spaced() + msg;
    }
}
