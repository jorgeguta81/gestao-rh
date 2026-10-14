package rh.servico;

import java.util.Comparator;
import java.util.List;
import rh.modelo.Funcionario;
import rh.modelo.TipoFuncionario;
import rh.repositorio.Repositorio;
import rh.util.Texto;

/** Calcula a folha de salários (o modelo não imprime nada: a apresentação é da visão). */
public class FolhaSalarioServico {

    /** Uma linha da folha. */
    public record LinhaFolha(int id, String nome, String tipo, double bruto, double inss, double irps,
                             double liquido, double inssPatronal) {
        /** Custo total para a empresa: bruto + INSS patronal. */
        public double custoEmpresa() { return bruto + inssPatronal; }
    }

    private final Repositorio<Funcionario> repo;

    public FolhaSalarioServico(Repositorio<Funcionario> repo) { this.repo = repo; }

    /** Polimorfismo em acção: cada tipo de funcionário calcula o seu bruto/INSS. */
    public List<LinhaFolha> calcular() { return calcular("", null); }

    /** Folha filtrada por parte do nome e/ou tipo de funcionário (null = todos). Só funcionários activos. */
    public List<LinhaFolha> calcular(String texto, TipoFuncionario tipo) {
        return repo.listarTodos().stream()
                .filter(Funcionario::isAtivo)
                .filter(f -> tipo == null || TipoFuncionario.de(f) == tipo)
                .filter(f -> Texto.contemTodas(f.getNome(), texto))
                .sorted(Comparator.comparingInt(Funcionario::getId))
                .map(f -> new LinhaFolha(f.getId(), f.getNome(), f.getTipo(),
                        f.calcularSalarioBruto(), f.calcularInss(), f.calcularIrps(),
                        f.calcularSalarioLiquido(), f.calcularInssPatronal()))
                .toList();
    }
}
