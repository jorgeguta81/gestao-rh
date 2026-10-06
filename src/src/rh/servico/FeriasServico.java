package rh.servico;

import java.time.LocalDate;
import java.util.List;
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
    }

    public int diasUsados(int funcionarioId, int ano) {
        return repo.listarTodos().stream()
                .filter(x -> x.getFuncionarioId() == funcionarioId)
                .filter(x -> x.getEstado() != EstadoFerias.REJEITADA)
                .filter(x -> x.getInicio().getYear() == ano)
                .mapToInt(Ferias::diasUteis).sum();
    }

    public List<Ferias> listar() { return repo.listarTodos(); }
    public boolean cancelar(int id) { return repo.remover(id); }
}
