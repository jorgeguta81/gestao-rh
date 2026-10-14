package rh.controlador;

import java.util.List;
import java.util.Optional;
import rh.modelo.Funcionario;
import rh.modelo.Permissao;
import rh.servico.CriterioFuncionario;
import rh.servico.DadosFuncionario;
import rh.servico.FuncionarioServico;

/** Controlador (MVC): funcionários. */
public class FuncionarioControlador {
    private final FuncionarioServico servico;
    private final Sessao sessao;

    public FuncionarioControlador(FuncionarioServico servico, Sessao sessao) {
        this.servico = servico;
        this.sessao = sessao;
    }

    public Funcionario admitir(DadosFuncionario d) {
        sessao.exigir(Permissao.FUNCIONARIOS_EDITAR);
        Funcionario f = servico.admitir(d);
        sessao.auditar("FUNCIONARIO_ADMITIDO id=" + f.getId());
        return f;
    }
    public Funcionario atualizar(int id, DadosFuncionario d) {
        sessao.exigir(Permissao.FUNCIONARIOS_EDITAR);
        Funcionario f = servico.atualizar(id, d);
        sessao.auditar("FUNCIONARIO_ACTUALIZADO id=" + id);
        return f;
    }
    public void demitir(int id) {
        sessao.exigir(Permissao.FUNCIONARIOS_EDITAR);
        servico.demitir(id);
        sessao.auditar("FUNCIONARIO_DEMITIDO id=" + id);
    }
    public void reativar(int id) {
        sessao.exigir(Permissao.FUNCIONARIOS_EDITAR);
        servico.reativar(id);
        sessao.auditar("FUNCIONARIO_REACTIVADO id=" + id);
    }
    public void remover(int id) {
        sessao.exigir(Permissao.FUNCIONARIOS_REMOVER);
        servico.remover(id);
        sessao.auditar("FUNCIONARIO_REMOVIDO id=" + id);
    }

    public List<Funcionario> listar() { sessao.exigir(Permissao.FUNCIONARIOS_VER); return servico.listar(); }

    public List<Funcionario> pesquisar(CriterioFuncionario criterio) {
        sessao.exigir(Permissao.FUNCIONARIOS_VER);
        return servico.pesquisar(criterio);
    }

    public Optional<Funcionario> buscar(int id) { sessao.exigir(Permissao.FUNCIONARIOS_VER); return servico.buscar(id); }

    /** Funcionários activos que a sessão pode escolher (todos para RH/Admin; só o próprio para os restantes). */
    public List<Funcionario> listarActivos() {
        sessao.exigirAutenticado();
        boolean todos = sessao.tem(Permissao.FERIAS_DECIDIR);
        return servico.listar().stream()
                .filter(Funcionario::isAtivo)
                .filter(f -> todos || f.getId() == sessao.funcionarioId())
                .toList();
    }

    /** Só para mostrar o nome. */
    public String nome(int id) { return servico.buscar(id).map(Funcionario::getNome).orElse("?"); }
}
