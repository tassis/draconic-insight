package dev.tassis.draconicclarity.format;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class EnergyText {
    private static final String[] UNITS = {"", "K", "M", "B", "T", "P", "E"};

    private EnergyText() {
    }

    public static String compact(long value) {
        if (value == 0) return "0";
        boolean negative = value < 0;
        BigDecimal amount = BigDecimal.valueOf(value).abs();
        int unit = 0;
        while (amount.compareTo(BigDecimal.valueOf(1_000)) >= 0 && unit < UNITS.length - 1) {
            amount = amount.movePointLeft(3);
            unit++;
        }
        int scale = amount.compareTo(BigDecimal.TEN) < 0 && amount.stripTrailingZeros().scale() > 0 ? 1 : 0;
        amount = amount.setScale(scale, RoundingMode.HALF_UP).stripTrailingZeros();
        return (negative ? "-" : "") + amount.toPlainString() + UNITS[unit];
    }

    public static String op(long value) {
        return compact(value) + " OP";
    }

    public static String opPerTick(long value) {
        return compact(value) + " OP/t";
    }
}
