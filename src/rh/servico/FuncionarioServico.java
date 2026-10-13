package rh.servico;

import java.util.List;
import java.util.Optional;
import rh.modelo.Ferias;
import rh.modelo.Funcionario;
import rh.repositorio.Repositorio;

public class FuncionarioServico {
    private final Repositorio<Funcionario> repo;
    private final Repositorio<Ferias> ferias;

    public FuncionarioServico(Repositorio<Funcionario> repo, Repositorio<Ferias> ferias) {
        this.repo = repo;
        this.ferias = ferias;
    }

    public Funcionario admitir(Funcionario f) {
        if (f.getNome() == null || f.getNome().isBlank())
            throw new IllegalArgumentException("O nome é obrigatório.");
        garantirBiUnico(f.getBi(), -1);
        return repo.criar(f);
    }

    public Funcionario admitir(DadosFuncionario d) { return admitir(d.criar()); }

    /** Actualiza um funcionário. Valida antes de alterar, para nunca ficar a meio. */
    public Funcionario atualizar(int id, DadosFuncionario d) {
        Funcionario f = obter(id);
        if (d.nome() == null || d.nome().isBlank()) throw new IllegalArgumentException("O nome é obrigatório.");
        garantirBiUnico(d.bi(), id);
        d.aplicarEm(f);
        repo.atualizar(f);
        return f;
    }

    public Optional<Funcionario> buscar(int id) { return repo.buscarPorId(id); }
    public List<Funcionario> listar() { return repo.listarTodos(); }

    /** Regra de negócio: demissão = desactivar (mantém o histórico). */
    public void demitir(int id) {
        Funcionario f = obter(id);
        if (!f.isAtivo()) throw new IllegalArgumentException("O funcionário já está inactivo.");
        f.setAtivo(false);
        repo.atualizar(f);
    }

    public void reativar(int id) {
        Funcionario f = obter(id);
        if (f.isAtivo()) throw new IllegalArgumentException("O funcionário já está activo.");
        f.setAtivo(true);
        repo.atualizar(f);
    }

    /** Remoção definitiva: só se não tiver histórico de férias (nesse caso use "Demitir"). */
    public void remover(int id) {
        obter(id);
        boolean temFerias = ferias.listarTodos().stream().anyMatch(x -> x.getFuncionarioId() == id);
        if (temFerias) throw new IllegalArgumentException(
                "Este funcionário tem pedidos de férias registados. Use \"Demitir\" para manter o histórico.");
        repo.remover(id);
    }

    private Funcionario obter(int id) {
        return repo.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado."));
    }

    private void garantirBiUnico(String bi, int idIgnorado) {
        boolean existe = repo.listarTodos().stream()
                .anyMatch(x -> x.getId() != idIgnorado && x.getBi() != null && x.getBi().equalsIgnoreCase(bi));
        if (existe) throw new IllegalArgumentException("Já existe um funcionário com este BI.");
    }
}
