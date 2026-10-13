package rh.config;

/**
 * Salário mínimo mensal por sector de actividade, em vigor desde 1 de Abril de 2026
 * (Diplomas Ministeriais 34 a 41/2026; tabela publicada pelo INSS).
 * Tabela PARCIAL: só inclui os sectores confirmados em fontes oficiais/oficiosas.
 * Para outros (ex.: indústria extractiva) consulte o INSS e acrescente aqui.
 */
public enum SectorActividade {
    AGRICULTURA("Agricultura, pecuária, caça, florestas e silvicultura", 7_072.00),
    PESCA_MARITIMA("Pesca marítima e semi-industrial", 7_063.22),
    INDUSTRIA_TRANSFORMADORA("Indústria transformadora", 10_622.50),
    ENERGIA_GRANDES("Electricidade, gás e água - grandes empresas", 12_775.00),
    ENERGIA_PME("Electricidade, gás e água - pequenas e médias empresas", 10_366.00),
    CONSTRUCAO("Construção civil", 8_652.00),
    SERVICOS_NAO_FINANCEIROS("Serviços não financeiros", 10_845.00),
    HOTELARIA("Hotelaria, turismo e similares", 10_600.00),
    SEGURANCA_PRIVADA("Segurança privada", 8_690.00),
    BANCA_SEGUROS("Bancos e seguradoras", 20_361.43),
    MICROFINANCAS("Microfinanças", 17_924.57);

    public static final SectorActividade PADRAO = SERVICOS_NAO_FINANCEIROS;

    private final String rotulo;
    private final double salarioMinimo;

    SectorActividade(String rotulo, double salarioMinimo) {
        this.rotulo = rotulo;
        this.salarioMinimo = salarioMinimo;
    }

    public double salarioMinimo() { return salarioMinimo; }

    @Override public String toString() { return rotulo; }
}
