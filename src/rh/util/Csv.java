package rh.util;

/** Escrita de CSV compatível com o Excel em português (separador ';'). */
public final class Csv {
    public static final char SEPARADOR = ';';

    private Csv() {}

    /**
     * Texto seguro para uma célula. Protege contra "injecção de fórmulas": um nome começado por
     * = + - ou @ seria executado como fórmula pelo Excel, por isso leva um apóstrofo à frente.
     */
    public static String texto(String s) {
        if (s == null) return "";
        String t = s.replace("\r", " ").replace("\n", " ");
        if (!t.isEmpty() && "=+-@\t".indexOf(t.charAt(0)) >= 0) t = "'" + t;
        if (t.indexOf(SEPARADOR) >= 0 || t.indexOf('"') >= 0) t = "\"" + t.replace("\"", "\"\"") + "\"";
        return t;
    }

    public static String linha(String... celulas) { return String.join(String.valueOf(SEPARADOR), celulas) + "\r\n"; }
}
