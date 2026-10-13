package rh.servico;

import java.util.List;
import java.util.Optional;
import rh.modelo.Departamento;
import rh.repositorio.Repositorio;

/** Regras de negócio dos departamentos. */
public class DepartamentoServico {
    private final Repositorio<Departamento> repo;

    public DepartamentoServico(Repositorio<Departamento> repo) { this.repo = repo; }

    public Departamento criar(Departamento d) { return repo.criar(d); }
    public Optional<Departamento> buscar(int id) { return repo.buscarPorId(id); }
    public List<Departamento> listar() { return repo.listarTodos(); }
}
