package rh.util;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

/** Regras de formato dos dados pessoais (documentos e contactos moçambicanos). */
public final class Validador {
    private Validador() {}

    private static final Pattern NOME = Pattern.compile("[\\p{L}][\\p{L} .'-]{2,99}");
    /** Bilhete de Identidade: 12 dígitos + 1 letra. */
    private static final Pattern BI = Pattern.compile("\\d{12}[A-Za-z]");
    /** NUIT: 9 dígitos. */
    private static final Pattern NUIT = Pattern.compile("\\d{9}");
    /** Telemóvel (82-87) com 9 dígitos, ou fixo de 8 dígitos começado por 2; prefixo +258 opcional. */
    private static final Pattern TELEFONE = Pattern.compile("(\\+?258)?(8[2-7]\\d{7}|2\\d{7})");
    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

    public static boolean nomeValido(String s) { return s != null && NOME.matcher(s.trim()).matches(); }
    public static boolean biValido(String s) { return s != null && BI.matcher(s.trim()).matches(); }
    public static boolean nuitValido(String s) { return s != null && NUIT.matcher(s.trim()).matches() && !s.trim().matches("0+"); }
    public static boolean telefoneValido(String s) { return s != null && TELEFONE.matcher(semEspacos(s)).matches(); }
    public static boolean emailValido(String s) { return s != null && EMAIL.matcher(s.trim()).matches(); }

    public static String semEspacos(String s) { return s == null ? "" : s.replaceAll("[\\s-]", ""); }

    /** Idade completa na data de referência. */
    public static int idade(LocalDate nascimento, LocalDate referencia) {
        return Period.between(nascimento, referencia).getYears();
    }

    public static boolean dinheiroValido(double v) { return !Double.isNaN(v) && !Double.isInfinite(v) && v >= 0; }
}
