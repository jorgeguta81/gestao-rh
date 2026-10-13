package rh.servico;

import java.util.List;
import rh.modelo.Funcionario;
import rh.repositorio.Repositorio;

/** Calcula a folha de salários (o modelo não imprime nada: a apresentação é da visão). */
public class FolhaSalarioServico {

    /** Uma linha da folha. */
    public record LinhaFolha(int id, String nome, String tipo, double bruto, double inss, double liquido) {}

    private final Repositorio<Funcionario> repo;

    public FolhaSalarioServico(Repositorio<Funcionario> repo) { this.repo = repo; }

    /** Polimorfismo em acção: cada tipo de funcionário calcula o seu bruto/INSS. */
    public List<LinhaFolha> calcular() {
        return repo.listarTodos().stream()
                .filter(Funcionario::isAtivo)
                .map(f -> new LinhaFolha(f.getId(), f.getNome(), f.getTipo(),
                        f.calcularSalarioBruto(), f.calcularInss(), f.calcularSalarioLiquido()))
                .toList();
    }
}
