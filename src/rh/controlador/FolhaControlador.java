package rh.controlador;

import java.util.List;
import rh.servico.FolhaSalarioServico;
import rh.servico.FolhaSalarioServico.LinhaFolha;

/** Controlador (MVC): folha de salários. */
public class FolhaControlador {
    private final FolhaSalarioServico servico;

    public FolhaControlador(FolhaSalarioServico servico) { this.servico = servico; }

    public List<LinhaFolha> linhas() { return servico.calcular(); }
}
