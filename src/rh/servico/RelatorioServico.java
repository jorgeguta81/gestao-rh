package rh.servico;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import rh.config.ParametrosLegais;
import rh.excecao.RecursoNaoEncontradoException;
import rh.modelo.*;
import rh.repositorio.Repositorio;
import rh.servico.FolhaSalarioServico.LinhaFolha;
import rh.util.Csv;
import rh.util.Formato;

/** Indicadores do dashboard, recibos de vencimento e relatórios exportáveis. Não altera dados. */
public class RelatorioServico {

    public record LinhaDepartamento(String nome, int activos, double massaBruta, double custoEmpresa) {}
    public record Totais(double bruto, double inss, double irps, double liquido, double inssPatronal, double custoEmpresa) {}
    public record ResumoFerias(int pendentes, int aprovadas, int rejeitadas, int emFeriasHoje, int proximos30Dias) {}

    /** Tudo o que o dashboard mostra. */
    public record Painel(String empresa, LocalDate data, int totalFuncionarios, int activos, int inactivos,
                         Map<TipoFuncionario, Integer> activosPorTipo, List<LinhaDepartamento> departamentos,
                         Totais folha, double salarioMedio, int admissoesUltimos12Meses,
                         ResumoFerias ferias, List<String> alertas) {}

    public enum Relatorio { FOLHA, FUNCIONARIOS, FERIAS }

    private final Repositorio<Funcionario> funcionarios;
    private final Repositorio<Departamento> departamentos;
    private final Repositorio<Ferias> ferias;
    private final Repositorio<Utilizador> utilizadores;
    private final DefinicoesServico definicoes;
    private final FolhaSalarioServico folha;

    public RelatorioServico(Repositorio<Funcionario> funcionarios, Repositorio<Departamento> departamentos,
                            Repositorio<Ferias> ferias, Repositorio<Utilizador> utilizadores,
                            DefinicoesServico definicoes, FolhaSalarioServico folha) {
        this.funcionarios = funcionarios;
        this.departamentos = departamentos;
        this.ferias = ferias;
        this.utilizadores = utilizadores;
        this.definicoes = definicoes;
        this.folha = folha;
    }

    // ------------------------------------------------------------------ dashboard
    public Painel painel(LocalDate hoje) {
        List<Funcionario> todos = funcionarios.listarTodos();
        List<Funcionario> activos = todos.stream().filter(Funcionario::isAtivo).toList();

        Map<TipoFuncionario, Integer> porTipo = new EnumMap<>(TipoFuncionario.class);
        for (TipoFuncionario t : TipoFuncionario.values()) porTipo.put(t, 0);
        activos.forEach(f -> porTipo.merge(TipoFuncionario.de(f), 1, Integer::sum));

        List<LinhaDepartamento> porDep = new ArrayList<>();
        for (Departamento d : departamentos.listarTodos()) {
            List<Funcionario> doDep = activos.stream().filter(f -> f.getDepartamentoId() == d.getId()).toList();
            porDep.add(new LinhaDepartamento(d.getNome(), doDep.size(),
                    doDep.stream().mapToDouble(Funcionario::calcularSalarioBruto).sum(),
                    doDep.stream().mapToDouble(f -> f.calcularSalarioBruto() + f.calcularInssPatronal()).sum()));
        }

        Totais totais = new Totais(
                activos.stream().mapToDouble(Funcionario::calcularSalarioBruto).sum(),
                activos.stream().mapToDouble(Funcionario::calcularInss).sum(),
                activos.stream().mapToDouble(Funcionario::calcularIrps).sum(),
                activos.stream().mapToDouble(Funcionario::calcularSalarioLiquido).sum(),
                activos.stream().mapToDouble(Funcionario::calcularInssPatronal).sum(),
                activos.stream().mapToDouble(f -> f.calcularSalarioBruto() + f.calcularInssPatronal()).sum());

        double media = activos.isEmpty() ? 0 : totais.bruto() / activos.size();
        int admissoes = (int) activos.stream().filter(f -> f.getDataAdmissao() != null
                && !f.getDataAdmissao().isAfter(hoje) && !f.getDataAdmissao().isBefore(hoje.minusMonths(12))).count();

        Set<Integer> idsActivos = new HashSet<>();
        activos.forEach(f -> idsActivos.add(f.getId()));
        List<Ferias> pedidos = ferias.listarTodos();
        int pend = 0, apr = 0, rej = 0, agora = 0, prox = 0;
        for (Ferias fe : pedidos) {
            switch (fe.getEstado()) {
                case PENDENTE -> pend++;
                case REJEITADA -> rej++;
                case APROVADA -> {
                    apr++;
                    if (idsActivos.contains(fe.getFuncionarioId())) {
                        if (!hoje.isBefore(fe.getInicio()) && !hoje.isAfter(fe.getFim())) agora++;
                        else if (fe.getInicio().isAfter(hoje) && !fe.getInicio().isAfter(hoje.plusDays(30))) prox++;
                    }
                }
            }
        }
        ResumoFerias rf = new ResumoFerias(pend, apr, rej, agora, prox);

        return new Painel(definicoes.nomeEmpresa(), hoje, todos.size(), activos.size(), todos.size() - activos.size(),
                porTipo, porDep, totais, media, admissoes, rf, alertas(activos, rf));
    }

    private List<String> alertas(List<Funcionario> activos, ResumoFerias rf) {
        List<String> a = new ArrayList<>();
        if (rf.pendentes() > 0) a.add(rf.pendentes() + " pedido(s) de férias a aguardar decisão.");
        double min = definicoes.salarioMinimo();
        long abaixo = activos.stream().filter(f -> f instanceof FuncionarioEfetivo e && e.getSalarioBase() < min).count();
        if (abaixo > 0) a.add(abaixo + " funcionário(s) com salário base abaixo do mínimo do sector (" + Formato.dinheiro(min) + " MT).");
        long incompletos = activos.stream().filter(f -> f.getNuit() == null || f.getDataNascimento() == null).count();
        if (incompletos > 0) a.add(incompletos + " funcionário(s) sem NUIT ou data de nascimento (ficha incompleta).");
        long bloqueadas = utilizadores.listarTodos().stream()
                .filter(u -> u.isAtivo() && u.getTentativasFalhadas() >= UtilizadorServico.MAX_TENTATIVAS).count();
        if (bloqueadas > 0) a.add(bloqueadas + " conta(s) de utilizador bloqueada(s) por tentativas falhadas.");
        return a;
    }

    // ------------------------------------------------------------------ recibo de vencimento
    /** Recibo em texto simples (ver Lei 13/2023, art. 123.º: identificação, período, remunerações e descontos). */
    public String recibo(int funcionarioId, YearMonth mes) {
        Funcionario f = funcionarios.buscarPorId(funcionarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Funcionário não encontrado."));
        DadosFuncionario d = DadosFuncionario.de(f);
        String dep = departamentos.buscarPorId(f.getDepartamentoId()).map(Departamento::getNome).orElse("?");
        String empresa = definicoes.nomeEmpresa().isBlank() ? "(empresa não definida)" : definicoes.nomeEmpresa();

        StringBuilder sb = new StringBuilder();
        String dupla = "=".repeat(58), simples = "-".repeat(58);
        sb.append(dupla).append('\n');
        sb.append(" RECIBO DE VENCIMENTO - ").append(Formato.mes(mes)).append('\n');
        sb.append(dupla).append('\n');
        sb.append(String.format(" Empresa:      %s%n", empresa));
        sb.append(String.format(" Trabalhador:  %s (n.º %d)%n", f.getNome(), f.getId()));
        sb.append(String.format(" BI: %s    NUIT: %s%n", f.getBi(), f.getNuit() == null ? "-" : f.getNuit()));
        sb.append(String.format(" Categoria:    %s (%s)%n", f.getCargo(), TipoFuncionario.de(f)));
        sb.append(String.format(" Departamento: %s    Admissão: %s%n", dep, f.getDataAdmissao()));
        sb.append(simples).append('\n');
        sb.append(" REMUNERAÇÕES\n");
        switch (d.tipo()) {
            case EFECTIVO -> { linha(sb, "Salário base", d.valor1()); linha(sb, "Subsídios", d.valor2()); }
            case GESTOR -> { linha(sb, "Salário base", d.valor1()); linha(sb, "Subsídios", d.valor2()); linha(sb, "Bónus", d.valor3()); }
            case CONTRATADO -> linha(sb, String.format(Formato.PT_MZ, "%d h x %s MT/h", (int) d.valor2(), Formato.dinheiro(d.valor1())), f.calcularSalarioBruto());
            case ESTAGIARIO -> linha(sb, "Bolsa de estágio", d.valor1());
        }
        linha(sb, "TOTAL BRUTO", f.calcularSalarioBruto());
        sb.append(simples).append('\n');
        sb.append(" DESCONTOS\n");
        linha(sb, String.format(Formato.PT_MZ, "INSS (%.0f%%)", ParametrosLegais.INSS_TRABALHADOR * 100), -f.calcularInss());
        linha(sb, "IRPS (retenção na fonte)", -f.calcularIrps());
        linha(sb, "TOTAL DESCONTOS", -(f.calcularInss() + f.calcularIrps()));
        sb.append(dupla).append('\n');
        linha(sb, "LÍQUIDO A RECEBER", f.calcularSalarioLiquido());
        sb.append(dupla).append('\n');
        sb.append(String.format(Formato.PT_MZ, " Contribuição da empresa para o INSS (%.0f%%): %s MT%n",
                ParametrosLegais.INSS_EMPREGADOR * 100, Formato.dinheiro(f.calcularInssPatronal())));
        sb.append(" IRPS calculado pela tabela mensal, sem dependentes.\n");
        return sb.toString();
    }

    private static void linha(StringBuilder sb, String descricao, double valor) {
        sb.append(String.format("   %-34s %17s MT%n", descricao, Formato.dinheiro(valor)));
    }

    // ------------------------------------------------------------------ exportações CSV
    public String csv(Relatorio r) {
        return switch (r) {
            case FOLHA -> csvFolha();
            case FUNCIONARIOS -> csvFuncionarios();
            case FERIAS -> csvFerias();
        };
    }

    private String csvFolha() {
        StringBuilder sb = new StringBuilder(Csv.linha("ID", "Nome", "Tipo", "Bruto", "INSS trabalhador", "IRPS",
                "Líquido", "INSS patronal", "Custo empresa"));
        double b = 0, i = 0, ir = 0, l = 0, p = 0, c = 0;
        for (LinhaFolha x : folha.calcular()) {
            sb.append(Csv.linha(String.valueOf(x.id()), Csv.texto(x.nome()), Csv.texto(x.tipo()), Formato.numeroCsv(x.bruto()),
                    Formato.numeroCsv(x.inss()), Formato.numeroCsv(x.irps()), Formato.numeroCsv(x.liquido()),
                    Formato.numeroCsv(x.inssPatronal()), Formato.numeroCsv(x.custoEmpresa())));
            b += x.bruto(); i += x.inss(); ir += x.irps(); l += x.liquido(); p += x.inssPatronal(); c += x.custoEmpresa();
        }
        sb.append(Csv.linha("", "TOTAL", "", Formato.numeroCsv(b), Formato.numeroCsv(i), Formato.numeroCsv(ir),
                Formato.numeroCsv(l), Formato.numeroCsv(p), Formato.numeroCsv(c)));
        return sb.toString();
    }

    private String csvFuncionarios() {
        StringBuilder sb = new StringBuilder(Csv.linha("ID", "Nome", "Tipo", "BI", "NUIT", "Cargo", "Departamento",
                "Admissão", "Estado", "Email", "Telefone", "Salário bruto"));
        for (Funcionario f : funcionarios.listarTodos()) {
            String dep = departamentos.buscarPorId(f.getDepartamentoId()).map(Departamento::getNome).orElse("?");
            sb.append(Csv.linha(String.valueOf(f.getId()), Csv.texto(f.getNome()), Csv.texto(f.getTipo()), Csv.texto(f.getBi()),
                    Csv.texto(f.getNuit()), Csv.texto(f.getCargo()), Csv.texto(dep), String.valueOf(f.getDataAdmissao()),
                    f.isAtivo() ? "Activo" : "Inactivo", Csv.texto(f.getEmail()), Csv.texto(f.getTelefone()),
                    Formato.numeroCsv(f.calcularSalarioBruto())));
        }
        return sb.toString();
    }

    private String csvFerias() {
        StringBuilder sb = new StringBuilder(Csv.linha("ID", "Funcionário", "Início", "Fim", "Dias de gozo", "Ano de referência", "Estado"));
        for (Ferias fe : ferias.listarTodos()) {
            String nome = funcionarios.buscarPorId(fe.getFuncionarioId()).map(Funcionario::getNome).orElse("?");
            sb.append(Csv.linha(String.valueOf(fe.getId()), Csv.texto(nome), String.valueOf(fe.getInicio()), String.valueOf(fe.getFim()),
                    String.valueOf(fe.getDiasGozo()), String.valueOf(fe.getAnoReferencia()), String.valueOf(fe.getEstado())));
        }
        return sb.toString();
    }
}
