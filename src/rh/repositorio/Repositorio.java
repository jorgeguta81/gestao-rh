package rh.repositorio;

import java.util.List;
import java.util.Optional;
import rh.modelo.Identificavel;

/** Interface genérica do CRUD. */
public interface Repositorio<T extends Identificavel> {
    T criar(T entidade);                     // Create
    Optional<T> buscarPorId(int id);         // Read
    List<T> listarTodos();                   // Read
    boolean atualizar(T entidade);           // Update
    boolean remover(int id);                 // Delete
}
