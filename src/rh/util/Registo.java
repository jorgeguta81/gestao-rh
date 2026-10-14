package rh.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

/**
 * Registo em ficheiros (pasta de dados):
 *  - rh.0.log       erros e avisos técnicos, com o detalhe (stack trace) para quem der suporte;
 *  - auditoria.0.log quem fez o quê e quando (admissões, demissões, logins...). Nunca guarda palavras-passe.
 */
public final class Registo {
    private static final Logger TECNICO = Logger.getLogger("rh.tecnico");
    private static final Logger AUDITORIA = Logger.getLogger("rh.auditoria");
    private static Path pasta;

    static {
        TECNICO.setUseParentHandlers(false);
        AUDITORIA.setUseParentHandlers(false);
        TECNICO.setLevel(Level.ALL);
        AUDITORIA.setLevel(Level.ALL);
    }

    private Registo() {}

    private static final Formatter FORMATO = new Formatter() {
        private final DateTimeFormatter data = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        @Override public String format(LogRecord r) {
            StringBuilder sb = new StringBuilder(data.format(LocalDateTime.now()))
                    .append(' ').append(r.getLevel().getName()).append(' ').append(formatMessage(r)).append('\n');
            if (r.getThrown() != null) {
                java.io.StringWriter sw = new java.io.StringWriter();
                r.getThrown().printStackTrace(new java.io.PrintWriter(sw));
                sb.append(sw);
            }
            return sb.toString();
        }
    };

    /** Liga os ficheiros de registo à pasta de dados (pode ser chamado mais de uma vez). */
    public static synchronized void iniciar(Path pastaDados) {
        pasta = pastaDados.toAbsolutePath();
        try {
            Files.createDirectories(pasta);
            trocar(TECNICO, new FileHandler(pasta.resolve("rh.%g.log").toString(), 1_000_000, 3, true), Level.INFO);
            trocar(AUDITORIA, new FileHandler(pasta.resolve("auditoria.%g.log").toString(), 1_000_000, 5, true), Level.INFO);
        } catch (IOException | SecurityException e) {
            // Sem registo em ficheiro a aplicação continua; os erros vão para a consola.
            TECNICO.setUseParentHandlers(true);
            System.err.println("Aviso: não foi possível abrir os ficheiros de registo: " + e.getMessage());
        }
    }

    private static void trocar(Logger l, Handler novo, Level nivel) {
        for (Handler h : l.getHandlers()) { l.removeHandler(h); h.close(); }
        novo.setFormatter(FORMATO);
        novo.setLevel(nivel);
        l.addHandler(novo);
    }

    public static Path ficheiroDeErros() { return pasta == null ? Path.of("rh.0.log") : pasta.resolve("rh.0.log"); }

    public static void info(String mensagem) { TECNICO.info(mensagem); }
    public static void aviso(String mensagem) { TECNICO.warning(mensagem); }
    public static void erro(String mensagem, Throwable causa) { TECNICO.log(Level.SEVERE, mensagem, causa); }

    /** Regista uma acção de um utilizador (sem dados sensíveis como palavras-passe, salários ou BI). */
    public static void auditar(String utilizador, String accao) { AUDITORIA.info(utilizador + " | " + accao); }
}
