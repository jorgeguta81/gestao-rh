package rh.controlador;

import java.time.LocalDate;
import java.util.List;
import rh.modelo.Ferias;
import rh.servico.FeriasServico;

/** Controlador (MVC): pedidos de férias. */
public class FeriasControlador {
    private final FeriasServico servico;

    public FeriasControlador(FeriasServico servico) { this.servico = servico; }

    public Ferias solicitar(int funcionarioId, LocalDate inicio, LocalDate fim) {
        return servico.solicitar(funcionarioId, inicio, fim);
    }
    public List<Ferias> listar() { return servico.listar(); }
}
