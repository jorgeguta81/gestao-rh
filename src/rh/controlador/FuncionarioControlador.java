package rh.controlador;

import java.util.List;
import java.util.Optional;
import rh.modelo.Funcionario;
import rh.modelo.Permissao;
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

    public Funcionario admitir(DadosFuncionario d) { sessao.exigir(Permissao.FUNCIONARIOS_EDITAR); return servico.admitir(d); }
    public Funcionario atualizar(int id, DadosFuncionario d) { sessao.exigir(Permissao.FUNCIONARIOS_EDITAR); return servico.atualizar(id, d); }
    public void demitir(int id) { sessao.exigir(Permissao.FUNCIONARIOS_EDITAR); servico.demitir(id); }
    public void reativar(int id) { sessao.exigir(Permissao.FUNCIONARIOS_EDITAR); servico.reativar(id); }
    public void remover(int id) { sessao.exigir(Permissao.FUNCIONARIOS_REMOVER); servico.remover(id); }

    public List<Funcionario> listar() { sessao.exigir(Permissao.FUNCIONARIOS_VER); return servico.listar(); }

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
