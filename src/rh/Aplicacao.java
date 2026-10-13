package rh;

import java.time.LocalDate;
import rh.controlador.*;
import rh.modelo.*;
import rh.repositorio.*;
import rh.servico.*;

/**
 * Raiz de composição: cria repositórios, serviços e controladores e liga-os.
 * As telas (visão) só conhecem os controladores.
 */
public final class Aplicacao {
    public final DepartamentoControlador departamentos;
    public final FuncionarioControlador funcionarios;
    public final FeriasControlador ferias;
    public final FolhaControlador folha;

    public Aplicacao() {
        Repositorio<Departamento> depRepo = new RepositorioMemoria<>();
        Repositorio<Funcionario> funcRepo = new RepositorioMemoria<>();
        Repositorio<Ferias> feriasRepo = new RepositorioMemoria<>();

        DepartamentoServico depServ = new DepartamentoServico(depRepo);
        FuncionarioServico funcServ = new FuncionarioServico(funcRepo);
        FeriasServico feriasServ = new FeriasServico(feriasRepo, funcRepo);
        FolhaSalarioServico folhaServ = new FolhaSalarioServico(funcRepo);

        departamentos = new DepartamentoControlador(depServ);
        funcionarios = new FuncionarioControlador(funcServ);
        ferias = new FeriasControlador(feriasServ);
        folha = new FolhaControlador(folhaServ);

        semear(depServ, funcServ);
    }

    private static void semear(DepartamentoServico dep, FuncionarioServico func) {
        dep.criar(new Departamento("Recursos Humanos", "Gestão de pessoal"));
        dep.criar(new Departamento("Financeiro", "Contabilidade e tesouraria"));
        func.admitir(new Gestor("Ana Macuácua", "110100000001A", "ana@empresa.co.mz", "841111111",
                "Directora RH", 1, LocalDate.of(2020, 3, 1), 60000, 5000, 10000));
        func.admitir(new FuncionarioEfetivo("Carlos Sitoe", "110100000002B", "carlos@empresa.co.mz", "842222222",
                "Contabilista", 2, LocalDate.of(2022, 6, 15), 35000, 3000));
        func.admitir(new FuncionarioContratado("Marta Langa", "110100000003C", "marta@empresa.co.mz", "843333333",
                "Consultora", 1, LocalDate.of(2024, 1, 10), 500, 120));
        func.admitir(new Estagiario("João Cossa", "110100000004D", "joao@empresa.co.mz", "844444444",
                "Estagiário", 2, LocalDate.of(2025, 2, 3), 8000));
    }
}
