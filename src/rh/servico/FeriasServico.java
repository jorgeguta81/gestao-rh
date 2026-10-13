package rh.servico;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import rh.config.ParametrosLegais;
import rh.excecao.ValidacaoException;
import rh.modelo.*;
import rh.repositorio.Repositorio;

/**
 * Regras de férias da Lei do Trabalho (Lei n.º 13/2023):
 * - 12 dias de calendário no 1.º ano de trabalho e 30 nos seguintes (art. 108.º);
 * - feriados dentro do período não contam como férias (art. 111.º);
 * - fraccionamento só em períodos de, pelo menos, 6 dias (art. 109.º);
 * - o direito de um ano pode gozar-se nesse ano ou no seguinte (art. 107.º).
 */
public class FeriasServico {

    /** Direito, dias já pedidos/gozados e saldo de um funcionário num ano de referência. */
    public record Saldo(int ano, int direito, int usados) {
        public int disponivel() { return direito - usados; }
    }

    private final Repositorio<Ferias> repo;
    private final Repositorio<Funcionario> funcionarios;

    public FeriasServico(Repositorio<Ferias> repo, Repositorio<Funcionario> funcionarios) {
        this.repo = repo;
        this.funcionarios = funcionarios;
    }

    /** Dias de calendário entre as datas (inclusive) menos os feriados. */
    public static int diasDeGozo(LocalDate inicio, LocalDate fim) {
        int total = (int) ChronoUnit.DAYS.between(inicio, fim) + 1;
        int feriados = 0;
        for (LocalDate d = inicio; !d.isAfter(fim); d = d.plusDays(1)) if (ParametrosLegais.ehFeriado(d)) feriados++;
        return total - feriados;
    }

    public Saldo saldo(int funcionarioId, int ano) {
        Funcionario f = obterFuncionario(funcionarioId);
        int anoAdmissao = f.getDataAdmissao().getYear();
        int direito = ano < anoAdmissao ? 0
                : ano == anoAdmissao ? ParametrosLegais.DIAS_FERIAS_PRIMEIRO_ANO : ParametrosLegais.DIAS_FERIAS_ANUAIS;
        return new Saldo(ano, direito, diasUsados(funcionarioId, ano));
    }

    public Ferias solicitar(int funcionarioId, LocalDate inicio, LocalDate fim, int anoReferencia) {
        Funcionario f = obterFuncionario(funcionarioId);
        if (!f.isAtivo()) throw new IllegalArgumentException("Funcionário inactivo.");
        if (inicio == null || fim == null) throw new ValidacaoException("Indique as datas de início e fim.");
        if (fim.isBefore(inicio)) throw new ValidacaoException("A data final é anterior à inicial.");
        if (anoReferencia != inicio.getYear() && anoReferencia != inicio.getYear() - 1)
            throw new ValidacaoException("O ano de referência tem de ser o ano de início das férias ou o anterior (art. 107.º).");

        boolean sobrepoe = repo.listarTodos().stream()
                .filter(x -> x.getFuncionarioId() == funcionarioId && x.getEstado() != EstadoFerias.REJEITADA)
                .anyMatch(x -> !fim.isBefore(x.getInicio()) && !inicio.isAfter(x.getFim()));
        if (sobrepoe) throw new IllegalArgumentException("Já existe um pedido de férias que se sobrepõe a este período.");

        int dias = diasDeGozo(inicio, fim);
        if (dias <= 0) throw new ValidacaoException("O período só tem feriados: não conta como férias.");

        Saldo s = saldo(funcionarioId, anoReferencia);
        if (dias > s.disponivel())
            throw new IllegalArgumentException(String.format(
                    "Excede o direito a férias de %d: tem %d dias, já usou/pediu %d e este pedido tem %d (os feriados não contam).",
                    anoReferencia, s.direito(), s.usados(), dias));
        if (dias < ParametrosLegais.MIN_DIAS_FRACAO_FERIAS && dias != s.disponivel())
            throw new IllegalArgumentException("Cada período de férias deve ter pelo menos "
                    + ParametrosLegais.MIN_DIAS_FRACAO_FERIAS + " dias (art. 109.º), excepto o último, que pode ser o saldo restante ("
                    + s.disponivel() + ").");
        return repo.criar(new Ferias(funcionarioId, inicio, fim, anoReferencia, dias));
    }

    public void decidir(int feriasId, boolean aprovar) {
        Ferias fe = repo.buscarPorId(feriasId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        if (fe.getEstado() != EstadoFerias.PENDENTE)
            throw new IllegalArgumentException("Pedido já foi decidido.");
        fe.setEstado(aprovar ? EstadoFerias.APROVADA : EstadoFerias.REJEITADA);
        repo.atualizar(fe); // grava a alteração
    }

    /** Dias de férias do ano de referência já pedidos ou aprovados (pedidos rejeitados não contam). */
    public int diasUsados(int funcionarioId, int anoReferencia) {
        return repo.listarTodos().stream()
                .filter(x -> x.getFuncionarioId() == funcionarioId)
                .filter(x -> x.getEstado() != EstadoFerias.REJEITADA)
                .filter(x -> x.getAnoReferencia() == anoReferencia)
                .mapToInt(Ferias::getDiasGozo).sum();
    }

    public List<Ferias> listar() { return repo.listarTodos(); }
    public Optional<Ferias> buscar(int id) { return repo.buscarPorId(id); }

    /** Só se cancelam pedidos pendentes, ou aprovados que ainda não começaram. */
    public void cancelar(int id) {
        Ferias fe = repo.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        boolean pendente = fe.getEstado() == EstadoFerias.PENDENTE;
        boolean aprovadaFutura = fe.getEstado() == EstadoFerias.APROVADA && fe.getInicio().isAfter(LocalDate.now());
        if (!pendente && !aprovadaFutura)
            throw new IllegalArgumentException("Só se podem cancelar pedidos pendentes ou aprovados que ainda não começaram.");
        repo.remover(id);
    }

    private Funcionario obterFuncionario(int id) {
        return funcionarios.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado."));
    }
}
