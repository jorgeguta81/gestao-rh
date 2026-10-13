package rh.repositorio;

import java.util.*;
import rh.modelo.Identificavel;

/** Implementação em memória. Mais tarde pode ser trocada por JDBC/MySQL sem mexer nos serviços. */
public class RepositorioMemoria<T extends Identificavel> implements Repositorio<T> {
    private final Map<Integer, T> dados = new LinkedHashMap<>();
    private int proximoId = 1;

    @Override
    public T criar(T entidade) {
        entidade.setId(proximoId++);
        dados.put(entidade.getId(), entidade);
        return entidade;
    }

    @Override
    public Optional<T> buscarPorId(int id) { return Optional.ofNullable(dados.get(id)); }

    @Override
    public List<T> listarTodos() { return new ArrayList<>(dados.values()); }

    @Override
    public boolean atualizar(T entidade) {
        if (!dados.containsKey(entidade.getId())) return false;
        dados.put(entidade.getId(), entidade);
        return true;
    }

    @Override
    public boolean remover(int id) { return dados.remove(id) != null; }
}
