package rh.controlador;

import rh.excecao.AcessoNegadoException;
import rh.modelo.*;

/** Quem está autenticado neste momento e o que pode fazer. */
public final class Sessao {
    private Utilizador utilizador;

    void iniciar(Utilizador u) { this.utilizador = u; }
    void terminar() { this.utilizador = null; }

    public boolean autenticado() { return utilizador != null; }
    public Utilizador utilizador() { return utilizador; }
    public Perfil perfil() { return utilizador == null ? null : utilizador.getPerfil(); }

    /** ID do funcionário associado à conta (0 se não houver). */
    public int funcionarioId() { return utilizador == null ? Utilizador.SEM_FUNCIONARIO : utilizador.getFuncionarioId(); }

    /** Regista no ficheiro de auditoria quem fez o quê (nunca dados sensíveis). */
    public void auditar(String accao) {
        rh.util.Registo.auditar(utilizador == null ? "-" : utilizador.getNomeUtilizador() + "(" + perfil() + ")", accao);
    }

    public boolean tem(Permissao p) { return utilizador != null && utilizador.getPerfil().permite(p); }

    public void exigirAutenticado() {
        if (!autenticado()) throw new AcessoNegadoException("Sessão não iniciada.");
    }

    /** Lança AcessoNegadoException se a permissão não existir. */
    public void exigir(Permissao p) {
        if (!autenticado()) throw new AcessoNegadoException("Sessão não iniciada.");
        if (!tem(p)) throw new AcessoNegadoException("O seu perfil (" + perfil() + ") não tem permissão para esta operação.");
    }
}
