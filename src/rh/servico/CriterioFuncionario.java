package rh.servico;

import rh.modelo.TipoFuncionario;

/**
 * Pesquisa, filtros e ordenação da lista de funcionários.
 * @param texto palavras a procurar em nome, BI, NUIT, cargo, email e telefone (sem acentos nem maiúsculas)
 * @param tipo null = todos os tipos
 * @param departamentoId 0 = todos
 */
public record CriterioFuncionario(String texto, TipoFuncionario tipo, int departamentoId, Estado estado,
                                  Campo ordenarPor, boolean ascendente) {

    public enum Estado {
        TODOS("Todos"), ACTIVOS("Activos"), INACTIVOS("Inactivos");
        private final String rotulo;
        Estado(String r) { rotulo = r; }
        @Override public String toString() { return rotulo; }
    }

    public enum Campo {
        NOME("Nome"), ID("ID"), ADMISSAO("Data de admissão"), SALARIO("Salário bruto"), CARGO("Cargo"), DEPARTAMENTO("Departamento");
        private final String rotulo;
        Campo(String r) { rotulo = r; }
        @Override public String toString() { return rotulo; }
    }

    /** Sem filtros, ordenado por nome. */
    public static CriterioFuncionario todos() {
        return new CriterioFuncionario("", null, 0, Estado.TODOS, Campo.NOME, true);
    }
}
