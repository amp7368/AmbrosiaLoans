package com.ambrosia.loans.discord.commands.base;

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
        if (error) prefix = "\u274C";
        else if (warning) prefix = "\u26A0";
        else prefix = "\u2705";
        return prefix + " " + msg;
    }
}
