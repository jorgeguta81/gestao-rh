package rh.servico;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import rh.config.ParametrosLegais;
import rh.excecao.RecursoNaoEncontradoException;
import rh.excecao.RegraNegocioException;
import rh.excecao.ValidacaoException;
import rh.modelo.*;
import rh.repositorio.Repositorio;
import rh.util.Texto;
import rh.util.Validador;

public class FuncionarioServico {
    private final Repositorio<Funcionario> repo;
    private final Repositorio<Ferias> ferias;
    private final Repositorio<Departamento> departamentos;
    private final DefinicoesServico definicoes;

    public FuncionarioServico(Repositorio<Funcionario> repo, Repositorio<Ferias> ferias,
                              Repositorio<Departamento> departamentos, DefinicoesServico definicoes) {
        this.repo = repo;
        this.ferias = ferias;
        this.departamentos = departamentos;
        this.definicoes = definicoes;
    }

    /** Admite um funcionário já construído (valida exactamente como se viesse do formulário). */
    public Funcionario admitir(Funcionario f) {
        validar(DadosFuncionario.de(f), -1);
        return repo.criar(f);
    }

    public Funcionario admitir(DadosFuncionario d) {
        DadosFuncionario limpo = normalizar(d);
        validar(limpo, -1);
        return repo.criar(limpo.criar());
    }

    /** Actualiza um funcionário. Valida antes de alterar, para nunca ficar a meio. */
    public Funcionario atualizar(int id, DadosFuncionario d) {
        Funcionario f = obter(id);
        DadosFuncionario limpo = normalizar(d);
        validar(limpo, id);
        limpo.aplicarEm(f);
        repo.atualizar(f);
        return f;
    }

    /** Pesquisa com filtros e ordenação (ver {@link CriterioFuncionario}). */
    public List<Funcionario> pesquisar(CriterioFuncionario c) {
        Comparator<Funcionario> ordem = switch (c.ordenarPor()) {
            case ID -> Comparator.comparingInt(Funcionario::getId);
            case ADMISSAO -> Comparator.comparing(Funcionario::getDataAdmissao, Comparator.nullsLast(Comparator.naturalOrder()));
            case SALARIO -> Comparator.comparingDouble(Funcionario::calcularSalarioBruto);
            case CARGO -> Comparator.comparing(f -> Texto.normalizar(f.getCargo()));
            case DEPARTAMENTO -> Comparator.comparing(f -> Texto.normalizar(nomeDepartamento(f.getDepartamentoId())));
            case NOME -> Comparator.comparing(f -> Texto.normalizar(f.getNome()));
        };
        if (!c.ascendente()) ordem = ordem.reversed();
        return repo.listarTodos().stream()
                .filter(f -> c.tipo() == null || TipoFuncionario.de(f) == c.tipo())
                .filter(f -> c.departamentoId() == 0 || f.getDepartamentoId() == c.departamentoId())
                .filter(f -> switch (c.estado()) {
                    case TODOS -> true;
                    case ACTIVOS -> f.isAtivo();
                    case INACTIVOS -> !f.isAtivo();
                })
                .filter(f -> Texto.contemTodas(String.join(" ", f.getNome(), f.getBi(), nuloVazio(f.getNuit()),
                        nuloVazio(f.getCargo()), nuloVazio(f.getEmail()), nuloVazio(f.getTelefone())), c.texto()))
                .sorted(ordem)
                .toList();
    }

    private String nomeDepartamento(int id) { return departamentos.buscarPorId(id).map(Departamento::getNome).orElse(""); }
    private static String nuloVazio(String s) { return s == null ? "" : s; }

    public Optional<Funcionario> buscar(int id) { return repo.buscarPorId(id); }
    public List<Funcionario> listar() { return repo.listarTodos(); }

    /** Regra de negócio: demissão = desactivar (mantém o histórico). */
    public void demitir(int id) {
        Funcionario f = obter(id);
        if (!f.isAtivo()) throw new RegraNegocioException("O funcionário já está inactivo.");
        f.setAtivo(false);
        repo.atualizar(f);
    }

    public void reativar(int id) {
        Funcionario f = obter(id);
        if (f.isAtivo()) throw new RegraNegocioException("O funcionário já está activo.");
        f.setAtivo(true);
        repo.atualizar(f);
    }

    /** Remoção definitiva: só se não tiver histórico de férias (nesse caso use "Demitir"). */
    public void remover(int id) {
        obter(id);
        boolean temFerias = ferias.listarTodos().stream().anyMatch(x -> x.getFuncionarioId() == id);
        if (temFerias) throw new RegraNegocioException(
                "Este funcionário tem pedidos de férias registados. Use \"Demitir\" para manter o histórico.");
        repo.remover(id);
    }

    private Funcionario obter(int id) {
        return repo.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Funcionário não encontrado."));
    }

    /** Tira espaços e uniformiza maiúsculas nos identificadores. */
    private static DadosFuncionario normalizar(DadosFuncionario d) {
        return new DadosFuncionario(d.tipo(), tira(d.nome()), tira(d.bi()).toUpperCase(), tira(d.nuit()),
                d.dataNascimento(), tira(d.email()), Validador.semEspacos(d.telefone()), tira(d.cargo()),
                d.departamentoId(), d.dataAdmissao(), d.valor1(), d.valor2(), d.valor3());
    }

    private static String tira(String s) { return s == null ? "" : s.trim(); }

    /** Valida tudo de uma vez e lança ValidacaoException com a lista completa de erros. */
    void validar(DadosFuncionario d, int idIgnorado) {
        List<String> e = new ArrayList<>();
        if (!Validador.nomeValido(d.nome())) e.add("Nome inválido (3 a 100 letras).");
        if (!Validador.biValido(d.bi())) e.add("BI inválido: devem ser 12 dígitos seguidos de 1 letra (ex.: 110100123456A).");
        if (!Validador.nuitValido(d.nuit())) e.add("NUIT inválido: devem ser 9 dígitos.");
        if (!Validador.telefoneValido(d.telefone())) e.add("Telefone inválido (ex.: 84 123 4567 ou +258841234567).");
        if (!d.email().isEmpty() && !Validador.emailValido(d.email())) e.add("Email inválido.");
        if (d.cargo().isBlank()) e.add("O cargo é obrigatório.");
        if (departamentos.buscarPorId(d.departamentoId()).isEmpty()) e.add("Departamento inexistente.");

        LocalDate hoje = LocalDate.now();
        if (d.dataNascimento() == null) e.add("A data de nascimento é obrigatória.");
        else if (d.dataAdmissao() != null
                && Validador.idade(d.dataNascimento(), d.dataAdmissao()) < ParametrosLegais.IDADE_MINIMA)
            e.add("Idade mínima de admissão: " + ParametrosLegais.IDADE_MINIMA + " anos (Lei do Trabalho, art. 29.º).");
        if (d.dataNascimento() != null && !d.dataNascimento().isBefore(hoje)) e.add("A data de nascimento tem de ser anterior a hoje.");
        if (d.dataAdmissao() == null) e.add("A data de admissão é obrigatória.");
        else if (d.dataAdmissao().isAfter(hoje.plusYears(1))) e.add("A data de admissão não pode estar a mais de 1 ano no futuro.");

        if (existeOutro(idIgnorado, x -> x.getBi() != null && x.getBi().equalsIgnoreCase(d.bi()))) e.add("Já existe um funcionário com este BI.");
        if (existeOutro(idIgnorado, x -> x.getNuit() != null && x.getNuit().equals(d.nuit()))) e.add("Já existe um funcionário com este NUIT.");

        validarValores(d, e);
        if (!e.isEmpty()) throw new ValidacaoException(e);
    }

    private void validarValores(DadosFuncionario d, List<String> e) {
        double[] v = {d.valor1(), d.valor2(), d.valor3()};
        for (double x : v) if (!Validador.dinheiroValido(x)) { e.add("Os valores monetários não podem ser negativos nem inválidos."); return; }
        switch (d.tipo()) {
            case EFECTIVO, GESTOR -> {
                double min = definicoes.salarioMinimo();
                if (d.valor1() < min) e.add("Salário base abaixo do salário mínimo do sector \"" + definicoes.sector() + "\" (" + rh.util.Formato.dinheiro(min) + " MT).");
            }
            case CONTRATADO -> {
                if (d.valor1() <= 0) e.add("O valor por hora tem de ser positivo.");
                if (d.valor2() > ParametrosLegais.MAX_HORAS_MES || d.valor2() != Math.floor(d.valor2()))
                    e.add("Horas trabalhadas: número inteiro até " + ParametrosLegais.MAX_HORAS_MES + " por mês.");
            }
            case ESTAGIARIO -> { if (d.valor1() <= 0) e.add("A bolsa tem de ser positiva."); }
        }
    }

    private boolean existeOutro(int idIgnorado, java.util.function.Predicate<Funcionario> p) {
        return repo.listarTodos().stream().anyMatch(x -> x.getId() != idIgnorado && p.test(x));
    }
}
