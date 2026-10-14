package rh;

import java.nio.file.Path;
import java.time.LocalDate;
import rh.controlador.*;
import rh.modelo.*;
import rh.repositorio.*;
import rh.servico.*;
import rh.util.Registo;

/**
 * Raiz de composição: cria repositórios, serviços e controladores e liga-os.
 * As telas (visão) só conhecem os controladores.
 */
public final class Aplicacao {
    public final Sessao sessao = new Sessao();
    public final UtilizadorControlador utilizadores;
    public final DefinicoesControlador definicoes;
    public final RelatorioControlador relatorios;
    public final DepartamentoControlador departamentos;
    public final FuncionarioControlador funcionarios;
    public final FeriasControlador ferias;
    public final FolhaControlador folha;

    /** Pasta dos dados: -Drh.dados=/caminho, ou ./dados por omissão. */
    public static Path pastaDados() { return Path.of(System.getProperty("rh.dados", "dados")); }

    public Aplicacao() { this(pastaDados()); }

    public Aplicacao(Path pasta) {
        Registo.iniciar(pasta);
        Repositorio<Departamento> depRepo = new RepositorioFicheiro<>(pasta.resolve("departamentos.dat"));
        Repositorio<Funcionario> funcRepo = new RepositorioFicheiro<>(pasta.resolve("funcionarios.dat"));
        Repositorio<Ferias> feriasRepo = new RepositorioFicheiro<>(pasta.resolve("ferias.dat"));
        Repositorio<Utilizador> utilRepo = new RepositorioFicheiro<>(pasta.resolve("utilizadores.dat"));

        DefinicoesServico defServ = new DefinicoesServico(pasta.resolve("definicoes.properties"));
        DepartamentoServico depServ = new DepartamentoServico(depRepo, funcRepo);
        FuncionarioServico funcServ = new FuncionarioServico(funcRepo, feriasRepo, depRepo, defServ);
        FeriasServico feriasServ = new FeriasServico(feriasRepo, funcRepo);
        FolhaSalarioServico folhaServ = new FolhaSalarioServico(funcRepo);
        UtilizadorServico utilServ = new UtilizadorServico(utilRepo, funcRepo);
        RelatorioServico relServ = new RelatorioServico(funcRepo, depRepo, feriasRepo, utilRepo, defServ, folhaServ);

        departamentos = new DepartamentoControlador(depServ, sessao);
        funcionarios = new FuncionarioControlador(funcServ, sessao);
        ferias = new FeriasControlador(feriasServ, sessao);
        folha = new FolhaControlador(folhaServ, relServ, sessao);
        utilizadores = new UtilizadorControlador(utilServ, sessao);
        definicoes = new DefinicoesControlador(defServ, sessao);
        relatorios = new RelatorioControlador(relServ, sessao);

        // Dados de exemplo só na primeira execução (quando ainda não há nada gravado)
        boolean primeiraVez = depServ.listar().isEmpty() && funcServ.listar().isEmpty();
        if (primeiraVez) semear(depServ, funcServ);
        if (utilServ.listar().isEmpty()) semearUtilizadores(utilServ, primeiraVez);
    }

    private static void semear(DepartamentoServico dep, FuncionarioServico func) {
        dep.criar(new Departamento("Recursos Humanos", "Gestão de pessoal"));
        dep.criar(new Departamento("Financeiro", "Contabilidade e tesouraria"));
        func.admitir(pessoa(new Gestor("Ana Macuácua", "110100000001A", "ana@empresa.co.mz", "841111111",
                "Directora RH", 1, LocalDate.of(2020, 3, 1), 60000, 5000, 10000), "400000001", LocalDate.of(1985, 4, 12)));
        func.admitir(pessoa(new FuncionarioEfetivo("Carlos Sitoe", "110100000002B", "carlos@empresa.co.mz", "842222222",
                "Contabilista", 2, LocalDate.of(2022, 6, 15), 35000, 3000), "400000002", LocalDate.of(1990, 9, 3)));
        func.admitir(pessoa(new FuncionarioContratado("Marta Langa", "110100000003C", "marta@empresa.co.mz", "843333333",
                "Consultora", 1, LocalDate.of(2024, 1, 10), 500, 120), "400000003", LocalDate.of(1992, 1, 25)));
        func.admitir(pessoa(new Estagiario("João Cossa", "110100000004D", "joao@empresa.co.mz", "844444444",
                "Estagiário", 2, LocalDate.of(2025, 2, 3), 8000), "400000004", LocalDate.of(2001, 11, 30)));
    }

    private static Funcionario pessoa(Funcionario f, String nuit, LocalDate nascimento) {
        f.setNuit(nuit);
        f.setDataNascimento(nascimento);
        return f;
    }

    /**
     * Contas iniciais. Todas obrigam a mudar a palavra-passe no primeiro acesso.
     * Em instalações reais só é criado o administrador.
     */
    private static void semearUtilizadores(UtilizadorServico us, boolean comDemonstracao) {
        us.criar("admin", "Admin@123", Perfil.ADMINISTRADOR, Utilizador.SEM_FUNCIONARIO, true);
        if (comDemonstracao) {
            us.criar("rh", "Rh@12345", Perfil.GESTOR_RH, 1, true);          // Ana Macuácua
            us.criar("carlos", "Func@1234", Perfil.FUNCIONARIO, 2, true);   // Carlos Sitoe
        }
    }
}
