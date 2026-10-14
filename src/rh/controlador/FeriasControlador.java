package rh.controlador;

import java.time.LocalDate;
import java.util.List;
import rh.excecao.AcessoNegadoException;
import rh.excecao.RecursoNaoEncontradoException;
import rh.modelo.Ferias;
import rh.modelo.Permissao;
import rh.servico.CriterioFerias;
import rh.servico.FeriasServico;

/** Controlador (MVC): pedidos de férias. */
public class FeriasControlador {
    private final FeriasServico servico;
    private final Sessao sessao;

    public FeriasControlador(FeriasServico servico, Sessao sessao) {
        this.servico = servico;
        this.sessao = sessao;
    }

    /** RH/Admin registam para qualquer funcionário; os restantes só para si. */
    public Ferias solicitar(int funcionarioId, LocalDate inicio, LocalDate fim, int anoReferencia) {
        sessao.exigir(Permissao.FERIAS_SOLICITAR);
        if (!sessao.tem(Permissao.FERIAS_DECIDIR) && funcionarioId != sessao.funcionarioId())
            throw new AcessoNegadoException("Só pode solicitar férias para si próprio.");
        Ferias f = servico.solicitar(funcionarioId, inicio, fim, anoReferencia);
        sessao.auditar("FERIAS_SOLICITADAS id=" + f.getId() + " funcionario=" + funcionarioId);
        return f;
    }

    /** Direito e saldo de férias de um funcionário (o próprio, ou qualquer um para RH/Admin). */
    public FeriasServico.Saldo saldo(int funcionarioId, int ano) {
        sessao.exigirAutenticado();
        if (!sessao.tem(Permissao.FERIAS_DECIDIR) && funcionarioId != sessao.funcionarioId())
            throw new AcessoNegadoException("Só pode consultar o seu próprio saldo de férias.");
        return servico.saldo(funcionarioId, ano);
    }

    /** Quem decide não pode decidir o seu próprio pedido (separação de funções). */
    public void decidir(int feriasId, boolean aprovar) {
        sessao.exigir(Permissao.FERIAS_DECIDIR);
        Ferias f = servico.buscar(feriasId).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));
        if (f.getFuncionarioId() == sessao.funcionarioId())
            throw new AcessoNegadoException("Não pode decidir o seu próprio pedido de férias.");
        servico.decidir(feriasId, aprovar);
        sessao.auditar((aprovar ? "FERIAS_APROVADAS" : "FERIAS_REJEITADAS") + " id=" + feriasId);
    }

    public void cancelar(int feriasId) {
        sessao.exigirAutenticado();
        Ferias f = servico.buscar(feriasId).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));
        if (!sessao.tem(Permissao.FERIAS_DECIDIR) && f.getFuncionarioId() != sessao.funcionarioId())
            throw new AcessoNegadoException("Só pode cancelar os seus próprios pedidos.");
        servico.cancelar(feriasId);
        sessao.auditar("FERIAS_CANCELADAS id=" + feriasId);
    }

    /** Pesquisa/filtros/ordenação; RH/Admin vêem todos, os restantes só os seus pedidos. */
    public List<Ferias> pesquisar(CriterioFerias criterio) {
        sessao.exigirAutenticado();
        boolean todas = sessao.tem(Permissao.FERIAS_VER_TODAS);
        return servico.pesquisar(criterio).stream().filter(f -> todas || f.getFuncionarioId() == sessao.funcionarioId()).toList();
    }

    /** RH/Admin vêem todos; os restantes só os seus. */
    public List<Ferias> listar() {
        sessao.exigirAutenticado();
        boolean todas = sessao.tem(Permissao.FERIAS_VER_TODAS);
        return servico.listar().stream().filter(f -> todas || f.getFuncionarioId() == sessao.funcionarioId()).toList();
    }
}
