package rh.controlador;

import java.util.List;
import rh.modelo.*;
import rh.servico.UtilizadorServico;

/** Controlador (MVC): início/fim de sessão e gestão de contas. */
public class UtilizadorControlador {
    private final UtilizadorServico servico;
    private final Sessao sessao;

    public UtilizadorControlador(UtilizadorServico servico, Sessao sessao) {
        this.servico = servico;
        this.sessao = sessao;
    }

    public Utilizador login(String nome, String password) {
        Utilizador u = servico.autenticar(nome, password);
        sessao.iniciar(u);
        return u;
    }

    public void terminarSessao() { sessao.terminar(); }

    /** Qualquer utilizador autenticado pode mudar a sua própria palavra-passe. */
    public void alterarPassword(String atual, String nova) {
        sessao.exigirAutenticado();
        servico.alterarPassword(sessao.utilizador().getId(), atual, nova);
    }

    public List<Utilizador> listar() { sessao.exigir(Permissao.UTILIZADORES_GERIR); return servico.listar(); }

    public Utilizador criar(String nome, String password, Perfil perfil, int funcionarioId) {
        sessao.exigir(Permissao.UTILIZADORES_GERIR);
        return servico.criar(nome, password, perfil, funcionarioId, true);
    }

    public Utilizador atualizar(int id, Perfil perfil, int funcionarioId, boolean ativo) {
        sessao.exigir(Permissao.UTILIZADORES_GERIR);
        if (id == sessao.utilizador().getId() && (!ativo || perfil != sessao.perfil()))
            throw new rh.excecao.AcessoNegadoException("Não pode desactivar nem alterar o perfil da sua própria conta.");
        return servico.atualizar(id, perfil, funcionarioId, ativo);
    }

    /** Devolve a palavra-passe temporária gerada, para o administrador a comunicar ao utilizador. */
    public String redefinirPassword(int id) {
        sessao.exigir(Permissao.UTILIZADORES_GERIR);
        return servico.redefinirPassword(id);
    }
}
