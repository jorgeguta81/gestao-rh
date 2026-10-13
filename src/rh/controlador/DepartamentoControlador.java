package rh.controlador;

import java.util.List;
import rh.modelo.Departamento;
import rh.servico.DepartamentoServico;

/** Controlador (MVC): faz a ponte entre as telas de departamentos e o serviço. */
public class DepartamentoControlador {
    private final DepartamentoServico servico;

    public DepartamentoControlador(DepartamentoServico servico) { this.servico = servico; }

    public List<Departamento> listar() { return servico.listar(); }

    public String nome(int id) { return servico.buscar(id).map(Departamento::getNome).orElse("?"); }
}
