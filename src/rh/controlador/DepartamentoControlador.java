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

    public Departamento criar(String nome, String descricao) {
        sessao.exigir(Permissao.DEPARTAMENTOS_EDITAR);
        return servico.criar(new Departamento(nome, descricao));
    }

    public Departamento atualizar(int id, String nome, String descricao) {
        sessao.exigir(Permissao.DEPARTAMENTOS_EDITAR);
        return servico.atualizar(id, nome, descricao);
    }

    public void remover(int id) { sessao.exigir(Permissao.DEPARTAMENTOS_EDITAR); servico.remover(id); }

    /** Só para mostrar o nome (não expõe dados sensíveis), por isso sem verificação de permissão. */
    public String nome(int id) { return servico.buscar(id).map(Departamento::getNome).orElse("?"); }
}
