package rh.evento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Barramento de eventos de domínio (padrão Observer).
 * Os serviços publicam ("Funcionário admitido", "Férias aprovadas"...) e a interface
 * subscreve para actualizar ecrãs e o registo de actividade.
 */
public class BarramentoEventos {

    public record Evento(LocalDateTime quando, String tipo, String descricao) {
        @Override public String toString() {
            return String.format("%tT  [%s] %s", quando, tipo, descricao);
        }
    }

    private final List<Consumer<Evento>> ouvintes = new CopyOnWriteArrayList<>();

    private final java.util.LinkedList<Evento> historico = new java.util.LinkedList<>();

    /** Últimos eventos (mais recente primeiro), para telas que abrem depois dos eventos. */
    public List<Evento> historico() { return new java.util.ArrayList<>(historico); }

    public void subscrever(Consumer<Evento> ouvinte) { ouvintes.add(ouvinte); }

    public void publicar(String tipo, String descricao) {
        Evento e = new Evento(LocalDateTime.now(), tipo, descricao);
        historico.addFirst(e);
        if (historico.size() > 200) historico.removeLast();
        ouvintes.forEach(o -> o.accept(e));
    }
}
