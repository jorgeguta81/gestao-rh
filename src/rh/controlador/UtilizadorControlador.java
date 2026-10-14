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
        try {
            Utilizador u = servico.autenticar(nome, password);
            sessao.iniciar(u);
            sessao.auditar("LOGIN_OK");
            return u;
        } catch (rh.excecao.AutenticacaoException e) {
            rh.util.Registo.auditar("-", "LOGIN_FALHADO utilizador=" + nome);
            throw e;
        }
    }

    public void terminarSessao() {
        if (sessao.autenticado()) sessao.auditar("LOGOUT");
        sessao.terminar();
    }

    /** Qualquer utilizador autenticado pode mudar a sua própria palavra-passe. */
    public void alterarPassword(String atual, String nova) {
        sessao.exigirAutenticado();
        servico.alterarPassword(sessao.utilizador().getId(), atual, nova);
        sessao.auditar("PASSWORD_ALTERADA");
    }

    public List<Utilizador> listar() { sessao.exigir(Permissao.UTILIZADORES_GERIR); return servico.listar(); }

    public List<Utilizador> pesquisar(String texto, Perfil perfil) {
        sessao.exigir(Permissao.UTILIZADORES_GERIR);
        return servico.pesquisar(texto, perfil);
    }

    public Utilizador criar(String nome, String password, Perfil perfil, int funcionarioId) {
        sessao.exigir(Permissao.UTILIZADORES_GERIR);
        Utilizador u = servico.criar(nome, password, perfil, funcionarioId, true);
        sessao.auditar("UTILIZADOR_CRIADO id=" + u.getId() + " perfil=" + perfil.name());
        return u;
    }

    public Utilizador atualizar(int id, Perfil perfil, int funcionarioId, boolean ativo) {
        sessao.exigir(Permissao.UTILIZADORES_GERIR);
        if (id == sessao.utilizador().getId() && (!ativo || perfil != sessao.perfil()))
            throw new rh.excecao.AcessoNegadoException("Não pode desactivar nem alterar o perfil da sua própria conta.");
        Utilizador u = servico.atualizar(id, perfil, funcionarioId, ativo);
        sessao.auditar("UTILIZADOR_ACTUALIZADO id=" + id + " perfil=" + perfil.name() + " activo=" + ativo);
        return u;
    }

    /** Devolve a palavra-passe temporária gerada, para o administrador a comunicar ao utilizador. */
    public String redefinirPassword(int id) {
        sessao.exigir(Permissao.UTILIZADORES_GERIR);
        String temp = servico.redefinirPassword(id);
        sessao.auditar("PASSWORD_REDEFINIDA id=" + id);
        return temp;
    }
}
