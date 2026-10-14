package rh.servico;

import java.util.List;
import java.util.Optional;
import rh.excecao.RecursoNaoEncontradoException;
import rh.excecao.RegraNegocioException;
import rh.excecao.ValidacaoException;
import rh.modelo.Departamento;
import rh.modelo.Funcionario;
import rh.repositorio.Repositorio;
import rh.util.Texto;

/** Regras de negócio dos departamentos. */
public class DepartamentoServico {
    private final Repositorio<Departamento> repo;
    private final Repositorio<Funcionario> funcionarios;

    public DepartamentoServico(Repositorio<Departamento> repo, Repositorio<Funcionario> funcionarios) {
        this.repo = repo;
        this.funcionarios = funcionarios;
    }

    public Departamento criar(Departamento d) {
        validar(d.getNome(), -1);
        return repo.criar(d);
    }

    public Departamento atualizar(int id, String nome, String descricao) {
        Departamento d = obter(id);
        validar(nome, id);
        d.setNome(nome.trim());
        d.setDescricao(descricao == null ? "" : descricao.trim());
        repo.atualizar(d);
        return d;
    }

    /** Não se remove um departamento que ainda tem funcionários. */
    public void remover(int id) {
        obter(id);
        long n = funcionarios.listarTodos().stream().filter(f -> f.getDepartamentoId() == id).count();
        if (n > 0) throw new RegraNegocioException(
                "O departamento ainda tem " + n + " funcionário(s). Transfira-os antes de o remover.");
        repo.remover(id);
    }

    /** Procura pelo nome ou descrição (sem acentos nem maiúsculas), ordenado por nome. */
    public List<Departamento> pesquisar(String texto) {
        return repo.listarTodos().stream()
                .filter(d -> Texto.contemTodas(d.getNome() + " " + d.getDescricao(), texto))
                .sorted(java.util.Comparator.comparing(d -> Texto.normalizar(d.getNome())))
                .toList();
    }

    public Optional<Departamento> buscar(int id) { return repo.buscarPorId(id); }
    public List<Departamento> listar() { return repo.listarTodos(); }

    private Departamento obter(int id) {
        return repo.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Departamento não encontrado."));
    }

    private void validar(String nome, int idIgnorado) {
        if (nome == null || nome.isBlank()) throw new ValidacaoException("O nome do departamento é obrigatório.");
        boolean repetido = repo.listarTodos().stream()
                .anyMatch(d -> d.getId() != idIgnorado && d.getNome().equalsIgnoreCase(nome.trim()));
        if (repetido) throw new RegraNegocioException("Já existe um departamento com este nome.");
    }
}
