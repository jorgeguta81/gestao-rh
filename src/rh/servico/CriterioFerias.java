package rh.servico;

import rh.modelo.EstadoFerias;

/**
 * Pesquisa e ordenação dos pedidos de férias.
 * @param texto parte do nome do funcionário
 * @param estado null = todos
 * @param ano null = todos; filtra pelo ano de início das férias
 */
public record CriterioFerias(String texto, EstadoFerias estado, Integer ano, Campo ordenarPor, boolean ascendente) {

    public enum Campo {
        INICIO("Data de início"), FUNCIONARIO("Funcionário"), ESTADO("Estado"), DIAS("Dias de gozo");
        private final String rotulo;
        Campo(String r) { rotulo = r; }
        @Override public String toString() { return rotulo; }
    }

    public static CriterioFerias todas() { return new CriterioFerias("", null, null, Campo.INICIO, false); }
}
