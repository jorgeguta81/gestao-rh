package rh;

import java.time.LocalDate;
import java.util.Scanner;
import rh.modelo.*;
import rh.repositorio.*;
import rh.servico.*;

public class Main {
    static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        Repositorio<Departamento> depRepo = new RepositorioMemoria<>();
        Repositorio<Funcionario> funcRepo = new RepositorioMemoria<>();
        Repositorio<Ferias> feriasRepo = new RepositorioMemoria<>();

        FuncionarioServico funcServ = new FuncionarioServico(funcRepo);
        FeriasServico feriasServ = new FeriasServico(feriasRepo, funcRepo);
        FolhaSalarioServico folhaServ = new FolhaSalarioServico(funcRepo);

        // Dados de exemplo
        depRepo.criar(new Departamento("Recursos Humanos", "Gestão de pessoal"));
        depRepo.criar(new Departamento("Financeiro", "Contabilidade e tesouraria"));
        funcServ.admitir(new Gestor("Ana Macuácua", "110100000001A", "ana@empresa.co.mz", "841111111",
                "Directora RH", 1, LocalDate.of(2020, 3, 1), 60000, 5000, 10000));
        funcServ.admitir(new FuncionarioEfetivo("Carlos Sitoe", "110100000002B", "carlos@empresa.co.mz", "842222222",
                "Contabilista", 2, LocalDate.of(2022, 6, 15), 35000, 3000));
        funcServ.admitir(new FuncionarioContratado("Marta Langa", "110100000003C", "marta@empresa.co.mz", "843333333",
                "Consultora", 1, LocalDate.of(2024, 1, 10), 500, 120));
        funcServ.admitir(new Estagiario("João Cossa", "110100000004D", "joao@empresa.co.mz", "844444444",
                "Estagiário", 2, LocalDate.of(2025, 2, 3), 8000));

        int op;
        do {
            System.out.println("\n===== GESTÃO DE RECURSOS HUMANOS =====");
            System.out.println("1. Departamentos   2. Funcionários   3. Férias   4. Folha de salários   0. Sair");
            op = lerInt("Opção: ");
            try {
                switch (op) {
                    case 1 -> menuDepartamentos(depRepo);
                    case 2 -> menuFuncionarios(funcServ, depRepo);
                    case 3 -> menuFerias(feriasServ);
                    case 4 -> folhaServ.imprimirFolha();
                    case 0 -> System.out.println("Até logo!");
                    default -> System.out.println("Opção inválida.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        } while (op != 0);
    }

    // ---------- Departamentos (CRUD) ----------
    static void menuDepartamentos(Repositorio<Departamento> repo) {
        System.out.println("1. Criar  2. Listar  3. Actualizar  4. Remover");
        switch (lerInt("Opção: ")) {
            case 1 -> System.out.println("Criado: " + repo.criar(new Departamento(lerTexto("Nome: "), lerTexto("Descrição: "))));
            case 2 -> repo.listarTodos().forEach(System.out::println);
            case 3 -> {
                Departamento d = repo.buscarPorId(lerInt("ID: ")).orElseThrow(() -> new IllegalArgumentException("Não encontrado."));
                d.setNome(lerTexto("Novo nome: "));
                d.setDescricao(lerTexto("Nova descrição: "));
                repo.atualizar(d);
                System.out.println("Actualizado.");
            }
            case 4 -> System.out.println(repo.remover(lerInt("ID: ")) ? "Removido." : "Não encontrado.");
            default -> System.out.println("Opção inválida.");
        }
    }

    // ---------- Funcionários (CRUD + negócio) ----------
    static void menuFuncionarios(FuncionarioServico serv, Repositorio<Departamento> depRepo) {
        System.out.println("1. Admitir  2. Listar  3. Actualizar contacto  4. Demitir  5. Remover  6. Registar horas (contratado)");
        switch (lerInt("Opção: ")) {
            case 1 -> {
                System.out.println("Tipo: 1-Efectivo 2-Gestor 3-Contratado 4-Estagiário");
                int tipo = lerInt("Tipo: ");
                String nome = lerTexto("Nome: "), bi = lerTexto("BI: "), email = lerTexto("Email: "),
                        tel = lerTexto("Telefone: "), cargo = lerTexto("Cargo: ");
                depRepo.listarTodos().forEach(System.out::println);
                int dep = lerInt("ID do departamento: ");
                if (depRepo.buscarPorId(dep).isEmpty()) throw new IllegalArgumentException("Departamento inexistente.");
                LocalDate hoje = LocalDate.now();
                Funcionario f = switch (tipo) {
                    case 1 -> new FuncionarioEfetivo(nome, bi, email, tel, cargo, dep, hoje,
                            lerDouble("Salário base: "), lerDouble("Subsídios: "));
                    case 2 -> new Gestor(nome, bi, email, tel, cargo, dep, hoje,
                            lerDouble("Salário base: "), lerDouble("Subsídios: "), lerDouble("Bónus: "));
                    case 3 -> new FuncionarioContratado(nome, bi, email, tel, cargo, dep, hoje,
                            lerDouble("Valor/hora: "), lerInt("Horas: "));
                    case 4 -> new Estagiario(nome, bi, email, tel, cargo, dep, hoje, lerDouble("Bolsa: "));
                    default -> throw new IllegalArgumentException("Tipo inválido.");
                };
                System.out.println("Admitido: " + serv.admitir(f));
            }
            case 2 -> serv.listar().forEach(System.out::println);
            case 3 -> {
                Funcionario f = serv.buscar(lerInt("ID: ")).orElseThrow(() -> new IllegalArgumentException("Não encontrado."));
                f.setEmail(lerTexto("Novo email: "));
                f.setTelefone(lerTexto("Novo telefone: "));
                serv.atualizar(f);
                System.out.println("Actualizado.");
            }
            case 4 -> { serv.demitir(lerInt("ID: ")); System.out.println("Funcionário desactivado."); }
            case 5 -> System.out.println(serv.remover(lerInt("ID: ")) ? "Removido." : "Não encontrado.");
            case 6 -> {
                Funcionario f = serv.buscar(lerInt("ID: ")).orElseThrow(() -> new IllegalArgumentException("Não encontrado."));
                if (f instanceof FuncionarioContratado c) {
                    c.setHorasTrabalhadas(lerInt("Total de horas: "));
                    System.out.println("Horas registadas.");
                } else System.out.println("Este funcionário não é contratado.");
            }
            default -> System.out.println("Opção inválida.");
        }
    }

    // ---------- Férias ----------
    static void menuFerias(FeriasServico serv) {
        System.out.println("1. Solicitar  2. Listar  3. Aprovar  4. Rejeitar  5. Cancelar");
        switch (lerInt("Opção: ")) {
            case 1 -> System.out.println("Pedido: " + serv.solicitar(lerInt("ID funcionário: "),
                    LocalDate.parse(lerTexto("Início (AAAA-MM-DD): ")), LocalDate.parse(lerTexto("Fim (AAAA-MM-DD): "))));
            case 2 -> serv.listar().forEach(System.out::println);
            case 3 -> { serv.decidir(lerInt("ID pedido: "), true); System.out.println("Aprovado."); }
            case 4 -> { serv.decidir(lerInt("ID pedido: "), false); System.out.println("Rejeitado."); }
            case 5 -> System.out.println(serv.cancelar(lerInt("ID pedido: ")) ? "Cancelado." : "Não encontrado.");
            default -> System.out.println("Opção inválida.");
        }
    }

    // ---------- Leitura de dados ----------
    static String lerTexto(String msg) { System.out.print(msg); return in.nextLine().trim(); }

    static int lerInt(String msg) {
        while (true) {
            try { return Integer.parseInt(lerTexto(msg)); }
            catch (NumberFormatException e) { System.out.println("Número inválido."); }
        }
    }

    static double lerDouble(String msg) {
        while (true) {
            try { return Double.parseDouble(lerTexto(msg).replace(',', '.')); }
            catch (NumberFormatException e) { System.out.println("Número inválido."); }
        }
    }
}
