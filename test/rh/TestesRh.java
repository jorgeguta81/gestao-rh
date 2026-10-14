package rh;

import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import rh.config.ParametrosLegais;
import rh.config.SectorActividade;
import rh.excecao.*;
import rh.modelo.*;
import rh.servico.*;
import rh.servico.CriterioFuncionario.Campo;
import rh.servico.CriterioFuncionario.Estado;

/**
 * Testes automáticos sem bibliotecas externas.
 * Compilar e correr (a partir da raiz do projecto):
 *   javac -encoding UTF-8 -d /tmp/rh-out $(find src test -name "*.java") && java -cp /tmp/rh-out rh.TestesRh
 * Termina com código 1 se algum teste falhar.
 */
public class TestesRh {
    private static int feitos = 0, falhados = 0;

    private interface Accao { void correr() throws Exception; }

    private static void ok(boolean condicao, String descricao) {
        feitos++;
        if (!condicao) { falhados++; System.out.println("  FALHOU: " + descricao); }
    }

    /** A acção tem de lançar a excepção indicada (e, se fragmento != null, a mensagem tem de o conter). */
    private static void lanca(Class<? extends Exception> tipo, String fragmento, Accao a, String descricao) {
        feitos++;
        try {
            a.correr();
            falhados++;
            System.out.println("  FALHOU (não lançou): " + descricao);
        } catch (Exception e) {
            boolean certo = tipo.isInstance(e) && (fragmento == null || String.valueOf(e.getMessage()).contains(fragmento));
            if (!certo) { falhados++; System.out.println("  FALHOU: " + descricao + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage()); }
        }
    }

    private static void seccao(String nome) { System.out.println("== " + nome); }

    private static Aplicacao nova() throws Exception {
        Aplicacao app = new Aplicacao(Files.createTempDirectory("rh-teste"));
        app.utilizadores.login("admin", "Admin@123");
        return app;
    }

    private static DadosFuncionario efectivo(String bi, String nuit, double salario) {
        return new DadosFuncionario(TipoFuncionario.EFECTIVO, "Teste Silva", bi, nuit, LocalDate.of(1995, 5, 5),
                "t@x.mz", "841234567", "Analista", 1, LocalDate.now(), salario, 1000, 0);
    }

    public static void main(String[] args) throws Exception {
        persistencia();
        crud();
        validacao();
        impostos();
        ferias();
        acesso();
        pesquisa();
        excepcoes();
        System.out.println("\n" + (feitos - falhados) + "/" + feitos + " verificações OK");
        System.exit(falhados == 0 ? 0 : 1);
    }

    // ------------------------------------------------------------------ persistência
    static void persistencia() throws Exception {
        seccao("Persistência");
        Path d = Files.createTempDirectory("rh-pers");
        Aplicacao a1 = new Aplicacao(d);
        a1.utilizadores.login("admin", "Admin@123");
        ok(a1.funcionarios.listar().size() == 4, "1.ª execução cria 4 funcionários de exemplo");
        Funcionario novo = a1.funcionarios.admitir(efectivo("110100000010E", "400000010", 20000));
        a1.funcionarios.demitir(novo.getId());

        Aplicacao a2 = new Aplicacao(d);
        a2.utilizadores.login("admin", "Admin@123");
        ok(a2.funcionarios.listar().size() == 5, "após reiniciar mantém os 5 (o exemplo não é duplicado)");
        ok(!a2.funcionarios.buscar(novo.getId()).get().isAtivo(), "a demissão sobreviveu ao reinício");
        ok(a2.departamentos.listar().size() == 2, "departamentos mantidos");
        Funcionario outro = a2.funcionarios.admitir(efectivo("110100000011F", "400000011", 20000));
        ok(outro.getId() == 6, "contador de IDs continua depois do reinício");

        Files.write(d.resolve("funcionarios.dat"), new byte[]{1, 2, 3, 4, 5});
        lanca(PersistenciaException.class, "danificado", () -> new Aplicacao(d), "ficheiro corrompido é detectado");
    }

    // ------------------------------------------------------------------ CRUD
    static void crud() throws Exception {
        seccao("CRUD e regras de integridade");
        Aplicacao app = nova();
        Funcionario f = app.funcionarios.admitir(efectivo("110100000020A", "400000020", 20000));
        int id = f.getId();
        lanca(ValidacaoException.class, "BI", () -> app.funcionarios.admitir(efectivo("110100000020A", "400000021", 20000)), "BI duplicado");
        lanca(ValidacaoException.class, "NUIT", () -> app.funcionarios.admitir(efectivo("110100000021A", "400000020", 20000)), "NUIT duplicado");

        app.funcionarios.atualizar(id, new DadosFuncionario(TipoFuncionario.EFECTIVO, "Nome Editado", "110100000020A", "400000020",
                LocalDate.of(1995, 5, 5), "t@x.mz", "841234567", "Sénior", 2, LocalDate.now(), 25000, 1000, 0));
        ok(app.funcionarios.buscar(id).get().getNome().equals("Nome Editado"), "editar grava o nome");
        ok(app.funcionarios.buscar(id).get().getDepartamentoId() == 2, "editar grava o departamento");
        lanca(RegraNegocioException.class, "inactivo", () -> { app.funcionarios.demitir(id); app.funcionarios.demitir(id); }, "demitir duas vezes");
        app.funcionarios.reativar(id);

        app.ferias.solicitar(id, LocalDate.now().plusDays(30), LocalDate.now().plusDays(40), LocalDate.now().plusDays(30).getYear());
        lanca(RegraNegocioException.class, "férias", () -> app.funcionarios.remover(id), "não remove quem tem férias");
        int pedido = app.ferias.listar().get(0).getId();
        app.ferias.decidir(pedido, true);
        app.ferias.cancelar(pedido);
        ok(app.ferias.listar().isEmpty(), "cancelar pedido aprovado futuro");
        app.funcionarios.remover(id);
        ok(app.funcionarios.buscar(id).isEmpty(), "remover funcionário sem férias");

        lanca(RegraNegocioException.class, "funcionário(s)", () -> app.departamentos.remover(1), "não remove departamento com pessoas");
        int dep = app.departamentos.criar("Logística", "x").getId();
        lanca(RegraNegocioException.class, "Já existe", () -> app.departamentos.criar("logística", ""), "departamento duplicado (ignora maiúsculas)");
        app.departamentos.remover(dep);
        ok(app.departamentos.nome(dep).equals("?"), "remover departamento vazio");
    }

    // ------------------------------------------------------------------ validação
    static void validacao() throws Exception {
        seccao("Validação de dados");
        Aplicacao app = nova();
        DadosFuncionario mau = new DadosFuncionario(TipoFuncionario.EFECTIVO, "X", "123", "12", LocalDate.now().minusYears(16),
                "sem-arroba", "99", "", 99, LocalDate.now(), 1000, 0, 0);
        try {
            app.funcionarios.admitir(mau);
            ok(false, "dados maus devem ser recusados");
        } catch (ValidacaoException e) {
            ok(e.getErros().size() >= 8, "devolve todos os erros de uma vez (" + e.getErros().size() + ")");
            String t = e.getMessage();
            ok(t.contains("Nome") && t.contains("BI") && t.contains("NUIT") && t.contains("Telefone") && t.contains("Email")
                    && t.contains("Idade mínima") && t.contains("cargo") && t.contains("Departamento") && t.contains("salário mínimo"),
                    "mensagem cobre nome, BI, NUIT, telefone, email, idade, cargo, departamento e salário mínimo");
        }
        lanca(ValidacaoException.class, "Idade mínima", () -> app.funcionarios.admitir(new DadosFuncionario(TipoFuncionario.EFECTIVO,
                "Jovem Teste", "110100000030A", "400000030", LocalDate.now().minusYears(17), "", "841234567", "Aprendiz", 1,
                LocalDate.now(), 20000, 0, 0)), "menor de 18 anos recusado (art. 29.º)");
        lanca(ValidacaoException.class, "salário mínimo", () -> app.funcionarios.admitir(efectivo("110100000031A", "400000031", 9000)),
                "salário abaixo do mínimo do sector (10.845 MT)");
        app.funcionarios.admitir(efectivo("110100000032A", "400000032", 10845));
        ok(true, "salário igual ao mínimo é aceite");
        app.definicoes.guardar("Banco Teste", SectorActividade.BANCA_SEGUROS);
        lanca(ValidacaoException.class, String.format("%,.2f", 20361.43), () -> app.funcionarios.admitir(efectivo("110100000033A", "400000033", 15000)),
                "mudar de sector muda o mínimo (banca: 20.361,43 MT)");
        ok(app.definicoes.nomeEmpresa().equals("Banco Teste") && app.definicoes.sector() == SectorActividade.BANCA_SEGUROS, "definições guardadas");
        app.funcionarios.admitir(new DadosFuncionario(TipoFuncionario.CONTRATADO, "Contratada Teste", "110100000034A", "400000034",
                LocalDate.of(1990, 1, 1), "", "+258841234567", "Consultora", 1, LocalDate.now(), 400, 100, 0));
        ok(true, "contratado com telefone +258 aceite");
        lanca(ValidacaoException.class, "Horas", () -> app.funcionarios.admitir(new DadosFuncionario(TipoFuncionario.CONTRATADO, "Contratada Dois",
                "110100000035A", "400000035", LocalDate.of(1990, 1, 1), "", "841234567", "Consultora", 1, LocalDate.now(), 400, 500, 0)),
                "horas mensais absurdas recusadas");

        ok(rh.util.Validador.biValido("110100123456A") && !rh.util.Validador.biValido("11010012345A"), "formato do BI");
        ok(rh.util.Validador.nuitValido("400123456") && !rh.util.Validador.nuitValido("000000000") && !rh.util.Validador.nuitValido("40012345"), "formato do NUIT");
        ok(rh.util.Validador.telefoneValido("84 123 4567") && rh.util.Validador.telefoneValido("21123456") && !rh.util.Validador.telefoneValido("881234567"), "formato do telefone");
        ok(rh.util.Validador.idade(LocalDate.of(2008, 6, 1), LocalDate.of(2026, 5, 31)) == 17
                && rh.util.Validador.idade(LocalDate.of(2008, 6, 1), LocalDate.of(2026, 6, 1)) == 18, "cálculo exacto da idade (18.º aniversário)");
    }

    // ------------------------------------------------------------------ INSS e IRPS
    static void impostos() throws Exception {
        seccao("INSS e IRPS (tabela mensal, 0 dependentes)");
        double[][] casos = {{20_000, 0}, {20_250, 0}, {21_000, 75}, {22_250, 200}, {32_750, 1_775},
                {60_750, 7_375}, {72_750, 10_375}, {144_750, 28_375}, {150_000, 30_055}};
        for (double[] c : casos) ok(Math.abs(ParametrosLegais.irps(c[0]) - c[1]) < 0.005, "IRPS de " + c[0] + " = " + c[1] + " (obteve " + ParametrosLegais.irps(c[0]) + ")");

        Aplicacao app = nova();
        Funcionario ana = app.funcionarios.buscar(1).get(), carlos = app.funcionarios.buscar(2).get();
        Funcionario marta = app.funcionarios.buscar(3).get(), joao = app.funcionarios.buscar(4).get();
        ok(ana.calcularSalarioBruto() == 75_000 && ana.calcularInss() == 2_250 && ana.calcularIrps() == 10_375
                && ana.calcularSalarioLiquido() == 62_375, "Ana (gestora, 75.000): INSS 2.250, IRPS 10.375, líquido 62.375");
        ok(ana.calcularInssPatronal() == 3_000, "INSS patronal de Ana = 4% = 3.000");
        ok(carlos.calcularInss() == 1_140 && carlos.calcularIrps() == 2_597 && carlos.calcularSalarioLiquido() == 34_263,
                "Carlos (38.000): INSS 1.140, IRPS 2.597, líquido 34.263");
        ok(marta.calcularSalarioBruto() == 60_000, "Marta (500/h x 120h) = 60.000");
        ok(joao.calcularInss() == 0 && joao.calcularIrps() == 0 && joao.calcularSalarioLiquido() == 8_000 && joao.calcularInssPatronal() == 0,
                "Estagiário (8.000): isento, líquido = bolsa");
        var linhas = app.folha.linhas();
        double totalLiq = linhas.stream().mapToDouble(l -> l.liquido()).sum();
        ok(linhas.size() == 4 && Math.abs(totalLiq - (62_375 + 34_263 + marta.calcularSalarioLiquido() + 8_000)) < 0.01, "folha soma as linhas");
    }

    // ------------------------------------------------------------------ férias
    static void ferias() throws Exception {
        seccao("Férias (Lei n.º 13/2023)");
        ok(ParametrosLegais.ehFeriado(LocalDate.of(2026, 1, 1)) && ParametrosLegais.ehFeriado(LocalDate.of(2026, 6, 25))
                && ParametrosLegais.ehFeriado(LocalDate.of(2026, 12, 25)), "feriados fixos reconhecidos");
        ok(!ParametrosLegais.ehFeriado(LocalDate.of(2024, 4, 7)) && ParametrosLegais.ehFeriado(LocalDate.of(2024, 4, 8)),
                "feriado ao domingo (7/Abr/2024) passa para segunda-feira");
        ok(rh.servico.FeriasServico.diasDeGozo(LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 31)) == 11, "20-31 Dez: 12 dias de calendário - Natal = 11");
        ok(rh.servico.FeriasServico.diasDeGozo(LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 15)) == 14, "dias de calendário incluem fins-de-semana (14)");

        Aplicacao app = nova();
        int carlos = 2, marta = 3;
        ok(app.ferias.saldo(carlos, 2026).direito() == 30, "admitido em 2022: 30 dias em 2026");
        ok(app.ferias.saldo(marta, 2024).direito() == 12, "admitida em 2024: 12 dias no 1.º ano");
        ok(app.ferias.saldo(marta, 2023).direito() == 0, "sem direito antes da admissão");
        ok(app.ferias.saldo(marta, 2025).direito() == 30, "30 dias a partir do 2.º ano");

        app.ferias.solicitar(carlos, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 15), 2026);
        ok(app.ferias.saldo(carlos, 2026).usados() == 14, "pedido de 14 dias descontado do saldo");
        lanca(RegraNegocioException.class, "sobrepõe", () -> app.ferias.solicitar(carlos, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20), 2026), "períodos sobrepostos");
        lanca(RegraNegocioException.class, "Excede", () -> app.ferias.solicitar(carlos, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 25), 2026), "excede o saldo (14 + 24 > 30)");
        lanca(RegraNegocioException.class, "pelo menos 6", () -> app.ferias.solicitar(carlos, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 6), 2026), "fracção inferior a 6 dias (art. 109.º)");
        app.ferias.solicitar(carlos, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 16), 2026);
        ok(app.ferias.saldo(carlos, 2026).disponivel() == 0, "gozar exactamente o saldo (30) é permitido");
        lanca(RegraNegocioException.class, "Excede", () -> app.ferias.solicitar(carlos, LocalDate.of(2027, 1, 5), LocalDate.of(2027, 1, 20), 2026), "saldo de 2026 esgotado mesmo gozando em 2027");
        app.ferias.solicitar(carlos, LocalDate.of(2027, 1, 5), LocalDate.of(2027, 1, 20), 2027);
        ok(true, "direito de 2027 é independente do de 2026");
        lanca(ValidacaoException.class, "ano de referência", () -> app.ferias.solicitar(carlos, LocalDate.of(2027, 8, 1), LocalDate.of(2027, 8, 10), 2020), "ano de referência fora do permitido");
        lanca(ValidacaoException.class, "anterior", () -> app.ferias.solicitar(carlos, LocalDate.of(2027, 9, 10), LocalDate.of(2027, 9, 1), 2027), "fim antes do início");
        lanca(ValidacaoException.class, "feriados", () -> app.ferias.solicitar(marta, LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 25), 2026), "período só de feriados");
        // rejeitar liberta o saldo
        int idPedido = app.ferias.listar().get(0).getId();
        app.ferias.decidir(idPedido, false);
        ok(app.ferias.saldo(carlos, 2026).usados() == 16, "pedido rejeitado deixa de contar");
        // último saldo inferior a 6 dias: só pode ser pedido de uma vez
        app.ferias.solicitar(carlos, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 10), 2026);   // 9 dias -> restam 5
        ok(app.ferias.saldo(carlos, 2026).disponivel() == 5, "restam 5 dias");
        lanca(RegraNegocioException.class, "pelo menos 6", () -> app.ferias.solicitar(carlos, LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 19), 2026), "4 dias de um saldo de 5 recusados");
        app.ferias.solicitar(carlos, LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 20), 2026);
        ok(app.ferias.saldo(carlos, 2026).disponivel() == 0, "os 5 dias finais são aceites de uma vez");
    }

    // ------------------------------------------------------------------ acesso
    static void acesso() throws Exception {
        seccao("Controlo de acesso");
        Aplicacao app = new Aplicacao(Files.createTempDirectory("rh-acesso"));
        lanca(AcessoNegadoException.class, "Sessão", () -> app.funcionarios.listar(), "sem login nada é permitido");
        lanca(AutenticacaoException.class, "incorrectos", () -> app.utilizadores.login("admin", "errada"), "password errada");
        lanca(AutenticacaoException.class, "incorrectos", () -> app.utilizadores.login("naoexiste", "x"), "utilizador inexistente dá a mesma mensagem");
        ok(app.utilizadores.login("admin", "Admin@123").isMudarPassword(), "primeiro acesso obriga a mudar a password");
        lanca(ValidacaoException.class, "8 caracteres", () -> app.utilizadores.alterarPassword("Admin@123", "fraca"), "password fraca recusada");
        app.utilizadores.alterarPassword("Admin@123", "NovaSenha1");
        ok(app.funcionarios.listar().size() == 4 && app.utilizadores.listar().size() == 3, "administrador vê tudo");
        lanca(AcessoNegadoException.class, "própria conta", () -> app.utilizadores.atualizar(1, Perfil.FUNCIONARIO, 0, true), "admin não se despromove");

        for (int i = 0; i < 5; i++) try { app.utilizadores.login("rh", "x"); } catch (AutenticacaoException e) { /* esperado */ }
        lanca(AutenticacaoException.class, "bloqueada", () -> app.utilizadores.login("rh", "Rh@12345"), "bloqueio após 5 falhas");
        String temp = app.utilizadores.redefinirPassword(2);
        app.utilizadores.terminarSessao();

        app.utilizadores.login("rh", temp);
        ok(app.funcionarios.listar().size() == 4, "RH vê funcionários");
        lanca(AcessoNegadoException.class, null, () -> app.funcionarios.remover(4), "RH não remove funcionários");
        lanca(AcessoNegadoException.class, null, () -> app.utilizadores.listar(), "RH não gere utilizadores");
        lanca(AcessoNegadoException.class, null, () -> app.definicoes.guardar("x", SectorActividade.CONSTRUCAO), "RH não altera definições");
        app.ferias.solicitar(2, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 15), 2026);
        app.ferias.solicitar(1, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 15), 2026);
        int propria = app.ferias.listar().stream().filter(f -> f.getFuncionarioId() == 1).findFirst().get().getId();
        int deCarlos = app.ferias.listar().stream().filter(f -> f.getFuncionarioId() == 2).findFirst().get().getId();
        lanca(AcessoNegadoException.class, "próprio pedido", () -> app.ferias.decidir(propria, true), "RH não aprova as suas férias");
        app.ferias.decidir(deCarlos, true);
        app.utilizadores.terminarSessao();

        app.utilizadores.login("carlos", "Func@1234");
        lanca(AcessoNegadoException.class, null, () -> app.funcionarios.listar(), "funcionário não lista colegas");
        lanca(AcessoNegadoException.class, null, () -> app.departamentos.listar(), "funcionário não lista departamentos");
        lanca(AcessoNegadoException.class, null, () -> app.funcionarios.admitir(efectivo("110100000040A", "400000040", 20000)), "funcionário não admite");
        ok(app.ferias.listar().size() == 1 && app.ferias.listar().get(0).getFuncionarioId() == 2, "vê só as suas férias");
        lanca(AcessoNegadoException.class, "si próprio", () -> app.ferias.solicitar(1, LocalDate.of(2027, 2, 2), LocalDate.of(2027, 2, 15), 2027), "não pede férias por outro");
        lanca(AcessoNegadoException.class, null, () -> app.ferias.decidir(deCarlos, true), "não decide férias");
        lanca(AcessoNegadoException.class, "próprio", () -> app.ferias.saldo(1, 2026), "não consulta saldo de outro");
        ok(app.folha.linhas().size() == 1 && app.folha.linhas().get(0).id() == 2, "vê só o seu recibo");
        ok(app.funcionarios.listarActivos().size() == 1, "lista de funcionários para férias só tem ele próprio");
        List<Utilizador> nada = null;
        try { nada = app.utilizadores.listar(); } catch (AcessoNegadoException e) { /* esperado */ }
        ok(nada == null, "funcionário não vê contas de utilizadores");
        app.utilizadores.terminarSessao();

        Aplicacao app2 = new Aplicacao(Path.of(System.getProperty("java.io.tmpdir")).resolve("inexistente-" + System.nanoTime()));
        ok(app2.utilizadores.login("admin", "Admin@123") != null, "(nova instalação) conta admin criada");
    }

    // ------------------------------------------------------------------ pesquisa
    private static String nomes(List<Funcionario> l) { return l.stream().map(f -> f.getNome().split(" ")[0]).collect(java.util.stream.Collectors.joining(",")); }
    private static CriterioFuncionario crit(String t, TipoFuncionario tipo, int dep, Estado e, Campo c, boolean asc) { return new CriterioFuncionario(t, tipo, dep, e, c, asc); }

    static void pesquisa() throws Exception {
        seccao("Pesquisa, filtros e ordenação");
        Aplicacao app = nova();
        var todos = CriterioFuncionario.todos();
        ok(nomes(app.funcionarios.pesquisar(todos)).equals("Ana,Carlos,João,Marta"), "por omissão: todos, por nome");
        ok(nomes(app.funcionarios.pesquisar(crit("macuacua", null, 0, Estado.TODOS, Campo.NOME, true))).equals("Ana"), "pesquisa ignora acentos e maiúsculas (macuacua -> Macuácua)");
        ok(nomes(app.funcionarios.pesquisar(crit("SITOE contab", null, 0, Estado.TODOS, Campo.NOME, true))).equals("Carlos"), "várias palavras em campos diferentes (nome + cargo)");
        ok(app.funcionarios.pesquisar(crit("ana carlos", null, 0, Estado.TODOS, Campo.NOME, true)).isEmpty(), "todas as palavras têm de coincidir na mesma pessoa");
        ok(nomes(app.funcionarios.pesquisar(crit("400000003", null, 0, Estado.TODOS, Campo.NOME, true))).equals("Marta"), "pesquisa por NUIT");
        ok(nomes(app.funcionarios.pesquisar(crit("", TipoFuncionario.ESTAGIARIO, 0, Estado.TODOS, Campo.NOME, true))).equals("João"), "filtro por tipo");
        ok(nomes(app.funcionarios.pesquisar(crit("", null, 1, Estado.TODOS, Campo.NOME, true))).equals("Ana,Marta"), "filtro por departamento");
        ok(nomes(app.funcionarios.pesquisar(crit("", TipoFuncionario.GESTOR, 2, Estado.TODOS, Campo.NOME, true))).isEmpty(), "filtros combinados (gestor no Financeiro: nenhum)");
        ok(nomes(app.funcionarios.pesquisar(crit("", null, 0, Estado.TODOS, Campo.SALARIO, true))).equals("João,Carlos,Marta,Ana"), "ordenar por salário crescente");
        ok(nomes(app.funcionarios.pesquisar(crit("", null, 0, Estado.TODOS, Campo.SALARIO, false))).equals("Ana,Marta,Carlos,João"), "ordenar por salário decrescente");
        ok(nomes(app.funcionarios.pesquisar(crit("", null, 0, Estado.TODOS, Campo.ADMISSAO, true))).equals("Ana,Carlos,Marta,João"), "ordenar por data de admissão");
        ok(nomes(app.funcionarios.pesquisar(crit("", null, 0, Estado.TODOS, Campo.DEPARTAMENTO, true))).equals("Carlos,João,Ana,Marta"), "ordenar por departamento (Financeiro antes de RH)");
        ok(nomes(app.funcionarios.pesquisar(crit("", null, 0, Estado.TODOS, Campo.ID, false))).equals("João,Marta,Carlos,Ana"), "ordenar por ID decrescente");
        app.funcionarios.demitir(4);
        ok(nomes(app.funcionarios.pesquisar(crit("", null, 0, Estado.INACTIVOS, Campo.NOME, true))).equals("João"), "filtro: inactivos");
        ok(app.funcionarios.pesquisar(crit("", null, 0, Estado.ACTIVOS, Campo.NOME, true)).size() == 3, "filtro: activos");

        // férias
        app.ferias.solicitar(2, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 15), 2026);
        app.ferias.solicitar(1, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 11), 2026);
        app.ferias.solicitar(3, LocalDate.of(2027, 1, 12), LocalDate.of(2027, 1, 23), 2027);
        int pedidoAna = app.ferias.listar().stream().filter(f -> f.getFuncionarioId() == 1).findFirst().get().getId();
        app.ferias.decidir(pedidoAna, true);
        var fr = new CriterioFerias("", null, null, CriterioFerias.Campo.INICIO, true);
        ok(app.ferias.pesquisar(fr).stream().map(f -> f.getFuncionarioId()).toList().equals(List.of(1, 2, 3)), "férias por data de início crescente");
        ok(app.ferias.pesquisar(new CriterioFerias("", null, null, CriterioFerias.Campo.INICIO, false)).get(0).getFuncionarioId() == 3, "férias por data de início decrescente");
        ok(app.ferias.pesquisar(new CriterioFerias("carlos", null, null, CriterioFerias.Campo.INICIO, true)).size() == 1, "férias: pesquisa por nome");
        ok(app.ferias.pesquisar(new CriterioFerias("", EstadoFerias.APROVADA, null, CriterioFerias.Campo.INICIO, true)).size() == 1, "férias: filtro por estado");
        ok(app.ferias.pesquisar(new CriterioFerias("", null, 2027, CriterioFerias.Campo.INICIO, true)).size() == 1, "férias: filtro por ano");
        ok(app.ferias.pesquisar(new CriterioFerias("", null, null, CriterioFerias.Campo.DIAS, false)).get(0).getDiasGozo() == 14, "férias: ordenar por dias (mais longo primeiro)");

        // folha, departamentos, utilizadores
        ok(app.folha.linhas("", TipoFuncionario.EFECTIVO).size() == 1 && app.folha.linhas("marta", null).size() == 1, "folha: filtro por tipo e nome");
        ok(app.folha.linhas().stream().noneMatch(l -> l.nome().startsWith("João")), "folha: inactivos não aparecem");
        ok(app.departamentos.pesquisar("FINANC").size() == 1 && app.departamentos.pesquisar("").size() == 2, "departamentos: pesquisa");
        ok(app.utilizadores.pesquisar("", Perfil.GESTOR_RH).size() == 1 && app.utilizadores.pesquisar("ADM", null).size() == 1, "utilizadores: pesquisa e filtro por perfil");

        // o perfil limita também as pesquisas
        app.utilizadores.terminarSessao();
        app.utilizadores.login("carlos", "Func@1234");
        ok(app.ferias.pesquisar(CriterioFerias.todas()).stream().allMatch(f -> f.getFuncionarioId() == 2) && app.ferias.pesquisar(CriterioFerias.todas()).size() == 1,
                "funcionário só encontra os seus pedidos");
        ok(app.ferias.pesquisar(new CriterioFerias("ana", null, null, CriterioFerias.Campo.INICIO, true)).isEmpty(), "funcionário não encontra pedidos de colegas pesquisando o nome");
        lanca(AcessoNegadoException.class, null, () -> app.funcionarios.pesquisar(todos), "funcionário não pesquisa funcionários");
        ok(app.folha.linhas("ana", null).isEmpty() && app.folha.linhas("", null).size() == 1, "funcionário só encontra o seu recibo");
    }

    // ------------------------------------------------------------------ excepções
    static void excepcoes() throws Exception {
        seccao("Tratamento de excepções");
        Aplicacao app = nova();
        lanca(RecursoNaoEncontradoException.class, "não encontrado", () -> app.funcionarios.demitir(999), "demitir ID inexistente");
        lanca(RecursoNaoEncontradoException.class, "não encontrado", () -> app.departamentos.remover(999), "remover departamento inexistente");
        lanca(RecursoNaoEncontradoException.class, "não encontrado", () -> app.ferias.cancelar(999), "cancelar pedido inexistente");
        lanca(RecursoNaoEncontradoException.class, "não encontrado", () -> app.utilizadores.redefinirPassword(999), "redefinir utilizador inexistente");
        ok(new ValidacaoException("x") instanceof RhException && new AcessoNegadoException("x") instanceof RhException
                && new PersistenciaException("x") instanceof RhException, "todas as excepções da aplicação derivam de RhException");

        // falha de gravação: o repositório desfaz a alteração em memória
        Path impossivel = Path.of("/proc/rh-inexistente/dados.dat");
        rh.repositorio.RepositorioFicheiro<Departamento> repo = new rh.repositorio.RepositorioFicheiro<>(impossivel);
        lanca(PersistenciaException.class, "gravar", () -> repo.criar(new Departamento("A", "b")), "falha ao gravar é comunicada");
        ok(repo.listarTodos().isEmpty(), "falha ao gravar: a criação é desfeita em memória (rollback)");

        // registos em ficheiro
        Path pasta = Files.createTempDirectory("rh-log");
        Aplicacao a = new Aplicacao(pasta);
        a.utilizadores.login("admin", "Admin@123");
        a.funcionarios.admitir(efectivo("110100000050A", "400000050", 20000));
        try { a.utilizadores.login("admin", "PalavraSecreta999"); } catch (AutenticacaoException e) { /* esperado */ }
        for (var h : java.util.logging.Logger.getLogger("rh.auditoria").getHandlers()) h.flush();
        String auditoria = Files.readString(pasta.resolve("auditoria.0.log"));
        ok(auditoria.contains("FUNCIONARIO_ADMITIDO") && auditoria.contains("LOGIN_OK") && auditoria.contains("LOGIN_FALHADO"),
                "auditoria regista admissão, login e login falhado");
        ok(!auditoria.contains("PalavraSecreta999") && !auditoria.contains("Admin@123") && !auditoria.contains("110100000050A"),
                "auditoria não contém palavras-passe nem BI");
    }
}
