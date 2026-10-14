package rh.util;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

/** Formatação para português de Moçambique, igual em qualquer computador (ex.: 75 000,50). */
public final class Formato {
    public static final Locale PT_MZ = Locale.of("pt", "MZ");

    private Formato() {}

    public static String dinheiro(double v) { return String.format(PT_MZ, "%,.2f", v); }

    /** Sem separador de milhares (para CSV). */
    public static String numeroCsv(double v) { return String.format(PT_MZ, "%.2f", v); }

    /** "outubro de 2026" */
    public static String mes(YearMonth m) {
        return m.getMonth().getDisplayName(TextStyle.FULL, PT_MZ) + " de " + m.getYear();
    }
}
