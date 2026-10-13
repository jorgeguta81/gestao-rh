package rh.controlador;

import java.util.List;
import java.util.Optional;
import rh.modelo.Funcionario;
import rh.servico.DadosFuncionario;
import rh.servico.FuncionarioServico;

/** Controlador (MVC): operações sobre funcionários pedidas pelas telas. */
public class FuncionarioControlador {
    private final FuncionarioServico servico;

    public FuncionarioControlador(FuncionarioServico servico) { this.servico = servico; }

    public Funcionario admitir(DadosFuncionario d) { return servico.admitir(d); }
    public Funcionario atualizar(int id, DadosFuncionario d) { return servico.atualizar(id, d); }
    public void demitir(int id) { servico.demitir(id); }
    public void reativar(int id) { servico.reativar(id); }
    public void remover(int id) { servico.remover(id); }
    public Optional<Funcionario> buscar(int id) { return servico.buscar(id); }
    public List<Funcionario> listar() { return servico.listar(); }
    public List<Funcionario> listarActivos() { return servico.listar().stream().filter(Funcionario::isAtivo).toList(); }

    public String nome(int id) { return servico.buscar(id).map(Funcionario::getNome).orElse("?"); }
}
