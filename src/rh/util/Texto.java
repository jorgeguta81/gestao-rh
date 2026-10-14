package rh.util;

import java.text.Normalizer;
import java.util.Locale;

/** Utilitários de texto para pesquisas. */
public final class Texto {
    private Texto() {}

    /** Minúsculas e sem acentos: "Macuácua" -> "macuacua". */
    public static String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    /** Todas as palavras da pesquisa têm de aparecer no texto (por qualquer ordem). Pesquisa vazia = tudo. */
    public static boolean contemTodas(String texto, String pesquisa) {
        String t = normalizar(texto);
        for (String palavra : normalizar(pesquisa).split("\\s+")) {
            if (!palavra.isEmpty() && !t.contains(palavra)) return false;
        }
        return true;
    }
}
