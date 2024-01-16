package com.ambrosia.loans.discord.base.emerald;

import static com.ambrosia.loans.discord.base.emerald.Emeralds.BLOCK;
import static com.ambrosia.loans.discord.base.emerald.Emeralds.LIQUID;
import static com.ambrosia.loans.discord.base.emerald.Emeralds.STACK;

import apple.utilities.util.Pretty;

public class EmeraldsFormatter {


    private boolean isBold = true;
    private int truncate = Integer.MAX_VALUE;
    private boolean includeTotal = false;
    private boolean inline = false;

    private EmeraldsFormatter() {
    }

    public static EmeraldsFormatter of() {
        return new EmeraldsFormatter();
    }

    public EmeraldsFormatter setBold(boolean isBold) {
        this.isBold = isBold;
        return this;
    }

    public EmeraldsFormatter setIncludeTotal(boolean includeTotal) {
        this.includeTotal = includeTotal;
        return this;
    }

    public EmeraldsFormatter setIncludeTotal() {
        this.includeTotal = true;
        return this;
    }

    public EmeraldsFormatter setTruncateFields(int truncate) {
        this.truncate = truncate;
        return this;
    }

    public EmeraldsFormatter setInline(boolean inline) {
        this.inline = inline;
        return this;
    }

    public EmeraldsFormatter setInline() {
        this.inline = true;
        return this;
    }

    public String format(Emeralds credits) {
        long creditsLeft = credits.amount();
        long stx = creditsLeft / STACK;
        creditsLeft -= stx * STACK;
        long le = creditsLeft / LIQUID;
        creditsLeft -= le * LIQUID;
        long eb = creditsLeft / BLOCK;
        creditsLeft -= eb * BLOCK;
        long e = creditsLeft;

        int fieldsLeft = truncate;

        StringBuilder message = new StringBuilder();
        if (stx != 0) fieldsLeft -= append(message, stx, "STX", fieldsLeft, false);
        if (le != 0) fieldsLeft -= append(message, le, "LE", fieldsLeft, false);
        if (eb != 0) fieldsLeft -= append(message, eb, "EB", fieldsLeft, false);
        if (e != 0 || message.isEmpty()) append(message, e, "E", fieldsLeft, true);

        if (includeTotal) {
            message.append(inline ? " " : "\n");
            String total = Pretty.commas(credits.amount());
            message.append(String.format("(**%s** total)", total));
        }

        return message.toString();
    }

    private int append(StringBuilder message, long amount, String unit, int fieldsLeft, boolean forceAdd) {
        if (!forceAdd && (amount == 0 || fieldsLeft == 0)) return fieldsLeft;
        if (!message.isEmpty()) message.append(", ");
        String format = isBold ? "**%s** " : "%s ";
        message.append(String.format(format, Pretty.commas(amount))).append(unit);
        return 1;
    }
}
