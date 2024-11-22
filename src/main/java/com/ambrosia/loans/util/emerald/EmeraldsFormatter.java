package com.ambrosia.loans.util.emerald;

import static com.ambrosia.loans.util.emerald.Emeralds.BLOCK;
import static com.ambrosia.loans.util.emerald.Emeralds.LIQUID;
import static com.ambrosia.loans.util.emerald.Emeralds.STACK;

import apple.utilities.util.Pretty;
import java.math.BigDecimal;
import java.math.MathContext;

public class EmeraldsFormatter {


    public static final EmeraldsFormatter STACKS = EmeraldsFormatter.of().setStacksOnly().setNoNegative();
    public static final EmeraldsFormatter PLUS_MINUS = EmeraldsFormatter.of().setPlusMinus();
    public static final EmeraldsFormatter EXACT = EmeraldsFormatter.of().setTruncateFields(1000);
    private boolean sign = false;
    private boolean isBold = false;
    private int truncate = 2;
    private boolean includeTotal = false;
    private boolean inline = false;
    private boolean stacksOnly = false;
    private boolean noNegativeSign = false;
    private boolean roundingEmeralds = false;

    private EmeraldsFormatter() {
    }

    public static EmeraldsFormatter of() {
        return new EmeraldsFormatter();
    }

    private EmeraldsFormatter setPlusMinus() {
        this.sign = true;
        return this;
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

    public EmeraldsFormatter setRounding(boolean roundingEmeralds) {
        this.roundingEmeralds = roundingEmeralds;
        return this;
    }

    public String format(long emeralds) {
        return format(Emeralds.of(emeralds));
    }

    public String format(Emeralds emeralds) {
        if (this.stacksOnly) {
            boolean removeNegative = emeralds.isNegative() && this.noNegativeSign;
            Emeralds ems = removeNegative ? emeralds.negative() : emeralds;

            return "%.2fSTX".formatted(ems.toStacks());
        }
        StringBuilder message = new StringBuilder();
        int fieldsLeft = truncate;
        boolean isNegative = emeralds.isNegative() && !this.noNegativeSign;

        long creditsLeft = Math.abs(emeralds.amount());
        long stx = creditsLeft / STACK;
        creditsLeft -= stx * STACK;
        if (stx != 0) fieldsLeft -= append(message, stx, fieldsLeft, EmeraldsUnit.STACKS, creditsLeft);

        long le = creditsLeft / LIQUID;
        creditsLeft -= le * LIQUID;
        if (le != 0) fieldsLeft -= append(message, le, fieldsLeft, EmeraldsUnit.LIQUIDS, creditsLeft);

        long eb = creditsLeft / BLOCK;
        creditsLeft -= eb * BLOCK;
        if (eb != 0) fieldsLeft -= append(message, eb, fieldsLeft, EmeraldsUnit.BLOCKS, creditsLeft);

        long e = creditsLeft;
        if (e != 0 || message.isEmpty()) {
            if (!roundingEmeralds || message.isEmpty())
                append(message, e, fieldsLeft, EmeraldsUnit.EMERALDS, creditsLeft);
        }

        if (includeTotal) {
            message.append(inline ? " " : "\n");
            String total = Pretty.commas(emeralds.amount());
            message.append(String.format("(**%s** total)", total));
        }
        if (isNegative) return "-" + message;
        if (sign) return "+" + message;
        return message.toString();
    }

    private int append(StringBuilder message, long amount, int fieldsLeft, EmeraldsUnit unit, long creditsLeft) {
        boolean forceAdd = unit.isBase() && message.isEmpty();
        if (!forceAdd && (amount == 0 || fieldsLeft == 0)) return fieldsLeft;
        if (!message.isEmpty()) message.append(", ");

        String amountPretty;
        boolean shouldConvert = (fieldsLeft == 1 && !unit.isBase()) || (roundingEmeralds && unit == EmeraldsUnit.BLOCKS);
        if (shouldConvert) {
            double totalInUnits = amount + unit.convert(creditsLeft);
            if (totalInUnits == (int) totalInUnits)
                amountPretty = String.valueOf((int) totalInUnits);
            else {
                int places = unit == EmeraldsUnit.BLOCKS ? 10 : 100;
                double val = Math.floor(totalInUnits * places) / places;
                String format = unit == EmeraldsUnit.BLOCKS ? "%.1f" : "%.2f";
                amountPretty = format.formatted(val);
            }
        } else amountPretty = Pretty.commas(amount);

        String format = isBold ? "**%s** " : "%s ";
        message.append(String.format(format, amountPretty)).append(unit);
        return 1;
    }

    public EmeraldsFormatter setStacksOnly() {
        this.stacksOnly = true;
        return this;
    }

    public EmeraldsFormatter setNoNegative() {
        this.noNegativeSign = true;
        return this;
    }

    private enum EmeraldsUnit {
        STACKS("STX", 64 * 64 * 64),
        LIQUIDS("LE", 64 * 64),
        BLOCKS("EB", 64),
        EMERALDS("E", 1);

        private final String unit;
        private final BigDecimal unitAmount;

        EmeraldsUnit(String unit, long unitAmount) {
            this.unit = unit;
            this.unitAmount = BigDecimal.valueOf(unitAmount);
        }

        @Override
        public String toString() {
            return unit;
        }

        public double convert(long credits) {
            return BigDecimal.valueOf(credits)
                .divide(unitAmount, MathContext.DECIMAL128)
                .doubleValue();
        }

        public boolean isBase() {
            return this == EMERALDS;
        }
    }
}
