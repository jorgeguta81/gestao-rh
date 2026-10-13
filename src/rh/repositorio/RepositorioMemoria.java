package rh.repositorio;

import java.util.*;
import rh.modelo.Identificavel;

/** Implementação em memória. Serve de base à versão que grava em ficheiro. */
public class RepositorioMemoria<T extends Identificavel> implements Repositorio<T> {
    protected final Map<Integer, T> dados = new LinkedHashMap<>();
    protected int proximoId = 1;

    @Override
    public synchronized T criar(T entidade) {
        entidade.setId(proximoId++);
        dados.put(entidade.getId(), entidade);
        aoAlterar();
        return entidade;
    }

    @Override
    public synchronized Optional<T> buscarPorId(int id) { return Optional.ofNullable(dados.get(id)); }

    @Override
    public synchronized List<T> listarTodos() { return new ArrayList<>(dados.values()); }

    @Override
    public synchronized boolean atualizar(T entidade) {
        if (!dados.containsKey(entidade.getId())) return false;
        dados.put(entidade.getId(), entidade);
        aoAlterar();
        return true;
    }

    @Override
    public synchronized boolean remover(int id) {
        boolean removido = dados.remove(id) != null;
        if (removido) aoAlterar();
        return removido;
    }

    /** Gancho chamado depois de cada alteração (a versão em ficheiro grava aqui). */
    protected void aoAlterar() {}
}
