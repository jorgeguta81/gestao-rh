package rh.servico;

import java.util.List;
import rh.modelo.Funcionario;
import rh.repositorio.Repositorio;

public class FolhaSalarioServico {
    private final Repositorio<Funcionario> repo;

    public FolhaSalarioServico(Repositorio<Funcionario> repo) { this.repo = repo; }

    /** Polimorfismo em acção: cada tipo calcula o seu bruto/INSS. */
    public void imprimirFolha() {
        List<Funcionario> activos = repo.listarTodos().stream().filter(Funcionario::isAtivo).toList();
        System.out.println("\n=== FOLHA DE SALÁRIOS ===");
        System.out.printf("%-4s %-22s %-11s %12s %10s %12s%n", "ID", "Nome", "Tipo", "Bruto", "INSS", "Líquido");
        double totalBruto = 0, totalLiquido = 0;
        for (Funcionario f : activos) {
            System.out.printf("%-4d %-22s %-11s %12.2f %10.2f %12.2f%n",
                    f.getId(), f.getNome(), f.getTipo(),
                    f.calcularSalarioBruto(), f.calcularInss(), f.calcularSalarioLiquido());
            totalBruto += f.calcularSalarioBruto();
            totalLiquido += f.calcularSalarioLiquido();
        }
        System.out.printf("TOTAL: Bruto %.2f MT | Líquido %.2f MT%n", totalBruto, totalLiquido);
    }
}
