package rh.servico;

import java.util.List;
import java.util.Optional;
import rh.modelo.Funcionario;
import rh.repositorio.Repositorio;

public class FuncionarioServico {
    private final Repositorio<Funcionario> repo;

    public FuncionarioServico(Repositorio<Funcionario> repo) { this.repo = repo; }

    public Funcionario admitir(Funcionario f) {
        if (f.getNome() == null || f.getNome().isBlank())
            throw new IllegalArgumentException("O nome é obrigatório.");
        boolean biExiste = repo.listarTodos().stream()
                .anyMatch(x -> x.getBi().equalsIgnoreCase(f.getBi()));
        if (biExiste) throw new IllegalArgumentException("Já existe um funcionário com este BI.");
        return repo.criar(f);
    }

    public Optional<Funcionario> buscar(int id) { return repo.buscarPorId(id); }
    public List<Funcionario> listar() { return repo.listarTodos(); }
    public boolean atualizar(Funcionario f) { return repo.atualizar(f); }
    public boolean remover(int id) { return repo.remover(id); }

    /** Regra de negócio: demissão = desactivar (mantém o histórico). */
    public void demitir(int id) {
        Funcionario f = repo.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado."));
        f.setAtivo(false);
    }
}
