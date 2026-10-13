package rh.controlador;

import java.util.List;
import rh.modelo.Funcionario;
import rh.servico.FuncionarioServico;

/** Controlador (MVC): operações sobre funcionários pedidas pelas telas. */
public class FuncionarioControlador {
    private final FuncionarioServico servico;

    public FuncionarioControlador(FuncionarioServico servico) { this.servico = servico; }

    public Funcionario admitir(Funcionario f) { return servico.admitir(f); }
    public List<Funcionario> listar() { return servico.listar(); }
    public List<Funcionario> listarActivos() { return servico.listar().stream().filter(Funcionario::isAtivo).toList(); }

    public String nome(int id) { return servico.buscar(id).map(Funcionario::getNome).orElse("?"); }
}
