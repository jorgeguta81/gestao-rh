package rh.repositorio;

import java.io.*;
import java.nio.file.*;
import java.util.LinkedHashMap;
import java.util.Map;
import rh.excecao.PersistenciaException;
import rh.modelo.Identificavel;

/**
 * Repositório que mantém tudo em memória e grava o ficheiro a cada alteração.
 * A gravação é atómica (ficheiro temporário + renomear), por isso uma falha a meio
 * nunca deixa o ficheiro de dados meio escrito.
 */
public class RepositorioFicheiro<T extends Identificavel> extends RepositorioMemoria<T> {

    /** Conteúdo gravado em disco. */
    private static final class Conteudo<E> implements Serializable {
        private static final long serialVersionUID = 1L;
        final LinkedHashMap<Integer, E> dados;
        final int proximoId;
        Conteudo(LinkedHashMap<Integer, E> dados, int proximoId) { this.dados = dados; this.proximoId = proximoId; }
    }

    /** Só aceita classes da própria aplicação e tipos básicos do Java ao ler o ficheiro. */
    private static final ObjectInputFilter FILTRO =
            ObjectInputFilter.Config.createFilter("rh.**;java.lang.*;java.util.*;java.time.*;!*");

    private final Path ficheiro;

    public RepositorioFicheiro(Path ficheiro) {
        this.ficheiro = ficheiro;
        carregar();
    }

    @SuppressWarnings("unchecked")
    private void carregar() {
        if (!Files.exists(ficheiro)) return;
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(ficheiro)))) {
            in.setObjectInputFilter(FILTRO);
            Conteudo<T> c = (Conteudo<T>) in.readObject();
            dados.putAll(c.dados);
            proximoId = c.proximoId;
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new PersistenciaException("Não foi possível ler o ficheiro de dados \"" + ficheiro
                    + "\". Está danificado ou foi criado por outra versão da aplicação.", e);
        }
    }

    @Override
    protected void aoAlterar() {
        try {
            Path pasta = ficheiro.toAbsolutePath().getParent();
            Files.createDirectories(pasta);
            Path temp = Files.createTempFile(pasta, "rh", ".tmp");
            try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(temp)))) {
                out.writeObject(new Conteudo<>(new LinkedHashMap<>(dados), proximoId));
            }
            try {
                Files.move(temp, ficheiro, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, ficheiro, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível gravar os dados em \"" + ficheiro + "\".", e);
        }
    }
}
