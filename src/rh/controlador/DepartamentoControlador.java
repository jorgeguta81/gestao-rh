package rh.controlador;

import java.util.List;
import rh.modelo.Departamento;
import rh.modelo.Permissao;
import rh.servico.DepartamentoServico;

/** Controlador (MVC): departamentos. */
public class DepartamentoControlador {
    private final DepartamentoServico servico;
    private final Sessao sessao;

    public DepartamentoControlador(DepartamentoServico servico, Sessao sessao) {
        this.servico = servico;
        this.sessao = sessao;
    }

    public List<Departamento> listar() { sessao.exigir(Permissao.DEPARTAMENTOS_VER); return servico.listar(); }

    public List<Departamento> pesquisar(String texto) { sessao.exigir(Permissao.DEPARTAMENTOS_VER); return servico.pesquisar(texto); }

    public Departamento criar(String nome, String descricao) {
        sessao.exigir(Permissao.DEPARTAMENTOS_EDITAR);
        Departamento d = servico.criar(new Departamento(nome, descricao));
        sessao.auditar("DEPARTAMENTO_CRIADO id=" + d.getId());
        return d;
    }

    public Departamento atualizar(int id, String nome, String descricao) {
        sessao.exigir(Permissao.DEPARTAMENTOS_EDITAR);
        Departamento d = servico.atualizar(id, nome, descricao);
        sessao.auditar("DEPARTAMENTO_ACTUALIZADO id=" + id);
        return d;
    }

    public void remover(int id) {
        sessao.exigir(Permissao.DEPARTAMENTOS_EDITAR);
        servico.remover(id);
        sessao.auditar("DEPARTAMENTO_REMOVIDO id=" + id);
    }

    /** Só para mostrar o nome (não expõe dados sensíveis), por isso sem verificação de permissão. */
    public String nome(int id) { return servico.buscar(id).map(Departamento::getNome).orElse("?"); }
}
