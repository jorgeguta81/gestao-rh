package rh.ui;

import java.time.LocalDate;
import rh.modelo.*;
import rh.repositorio.*;
import rh.servico.*;

/** Guarda os repositórios e serviços partilhados por todas as telas (os mesmos dados em todo o lado). */
public final class Contexto {
    public static final Repositorio<Departamento> departamentos = new RepositorioMemoria<>();
    public static final Repositorio<Funcionario> repoFuncionarios = new RepositorioMemoria<>();
    public static final Repositorio<Ferias> repoFerias = new RepositorioMemoria<>();

    public static final FuncionarioServico funcionarios = new FuncionarioServico(repoFuncionarios);
    public static final FeriasServico ferias = new FeriasServico(repoFerias, repoFuncionarios);

    static {
        // Dados de exemplo (os mesmos do Main de consola)
        departamentos.criar(new Departamento("Recursos Humanos", "Gestão de pessoal"));
        departamentos.criar(new Departamento("Financeiro", "Contabilidade e tesouraria"));
        funcionarios.admitir(new Gestor("Ana Macuácua", "110100000001A", "ana@empresa.co.mz", "841111111",
                "Directora RH", 1, LocalDate.of(2020, 3, 1), 60000, 5000, 10000));
        funcionarios.admitir(new FuncionarioEfetivo("Carlos Sitoe", "110100000002B", "carlos@empresa.co.mz", "842222222",
                "Contabilista", 2, LocalDate.of(2022, 6, 15), 35000, 3000));
        funcionarios.admitir(new FuncionarioContratado("Marta Langa", "110100000003C", "marta@empresa.co.mz", "843333333",
                "Consultora", 1, LocalDate.of(2024, 1, 10), 500, 120));
        funcionarios.admitir(new Estagiario("João Cossa", "110100000004D", "joao@empresa.co.mz", "844444444",
                "Estagiário", 2, LocalDate.of(2025, 2, 3), 8000));
    }

    private Contexto() {}

    public static String nomeDepartamento(int id) {
        return departamentos.buscarPorId(id).map(Departamento::getNome).orElse("?");
    }

    public static String nomeFuncionario(int id) {
        return repoFuncionarios.buscarPorId(id).map(Funcionario::getNome).orElse("?");
    }
}
