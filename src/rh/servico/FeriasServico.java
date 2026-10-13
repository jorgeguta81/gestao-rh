package rh.servico;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import rh.modelo.*;
import rh.repositorio.Repositorio;

public class FeriasServico {
    public static final int LIMITE_DIAS_UTEIS_ANO = 22; // ajuste conforme a política da empresa

    private final Repositorio<Ferias> repo;
    private final Repositorio<Funcionario> funcionarios;

    public FeriasServico(Repositorio<Ferias> repo, Repositorio<Funcionario> funcionarios) {
        this.repo = repo;
        this.funcionarios = funcionarios;
    }

    public Ferias solicitar(int funcionarioId, LocalDate inicio, LocalDate fim) {
        Funcionario f = funcionarios.buscarPorId(funcionarioId)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado."));
        if (!f.isAtivo()) throw new IllegalArgumentException("Funcionário inactivo.");
        if (fim.isBefore(inicio)) throw new IllegalArgumentException("Data final anterior à inicial.");

        Ferias nova = new Ferias(funcionarioId, inicio, fim);
        int jaUsados = diasUsados(funcionarioId, inicio.getYear());
        if (jaUsados + nova.diasUteis() > LIMITE_DIAS_UTEIS_ANO)
            throw new IllegalArgumentException("Excede o limite de " + LIMITE_DIAS_UTEIS_ANO
                    + " dias úteis (já usados/pedidos: " + jaUsados + ").");
        return repo.criar(nova);
    }

    public void decidir(int feriasId, boolean aprovar) {
        Ferias fe = repo.buscarPorId(feriasId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        if (fe.getEstado() != EstadoFerias.PENDENTE)
            throw new IllegalArgumentException("Pedido já foi decidido.");
        fe.setEstado(aprovar ? EstadoFerias.APROVADA : EstadoFerias.REJEITADA);
        repo.atualizar(fe); // grava a alteração
    }

    public int diasUsados(int funcionarioId, int ano) {
        return repo.listarTodos().stream()
                .filter(x -> x.getFuncionarioId() == funcionarioId)
                .filter(x -> x.getEstado() != EstadoFerias.REJEITADA)
                .filter(x -> x.getInicio().getYear() == ano)
                .mapToInt(Ferias::diasUteis).sum();
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
}
