package rh.config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

/**
 * Valores fixados na lei. Cada constante indica a fonte; se a lei mudar, altera-se SÓ aqui.
 * Ver docs/LEGISLACAO.md para o resumo e os pontos a confirmar com um contabilista/jurista.
 */
public final class ParametrosLegais {
    private ParametrosLegais() {}

    // ---------- Segurança social (INSS) ----------
    /** Contribuição do trabalhador: 3% da remuneração (Regulamento da Segurança Social Obrigatória; site do INSS). */
    public static final double INSS_TRABALHADOR = 0.03;
    /** Contribuição da entidade empregadora: 4% (idem). */
    public static final double INSS_EMPREGADOR = 0.04;
    /**
     * Se a contribuição do trabalhador reduz a base do IRPS.
     * CONFIRMAR com o contabilista: as fontes consultadas divergem. Está activo (prática corrente em folhas de salário).
     */
    public static final boolean INSS_REDUZ_BASE_IRPS = true;

    // ---------- IRPS - trabalho dependente: tabela mensal de retenção na fonte (art. 65-A do Código do IRPS, Lei 11/2025) ----------
    /** Escalão: acima de limiteInferior paga parcelaFixa + taxa * (base - limiteInferior). Tabela para 0 dependentes. */
    public record Escalao(double limiteInferior, double parcelaFixa, double taxa) {}

    public static final List<Escalao> IRPS_MENSAL = List.of(
            new Escalao(20_250.00, 0.00, 0.10),
            new Escalao(22_250.00, 200.00, 0.15),
            new Escalao(32_750.00, 1_775.00, 0.20),
            new Escalao(60_750.00, 7_375.00, 0.25),
            new Escalao(144_750.00, 28_375.00, 0.32));

    /** IRPS mensal a reter sobre uma base mensal (isento até 20.250 MT). */
    public static double irps(double baseMensal) {
        Escalao e = null;
        for (Escalao x : IRPS_MENSAL) if (baseMensal >= x.limiteInferior()) e = x;
        if (e == null) return 0;
        return arredondar(e.parcelaFixa() + e.taxa() * (baseMensal - e.limiteInferior()));
    }

    // ---------- Lei do Trabalho (Lei n.º 13/2023, de 25 de Agosto) ----------
    /** Art. 29.º: idade mínima de admissão (15 a 18 só com autorização especial, não suportada). */
    public static final int IDADE_MINIMA = 18;
    /** Art. 108.º: férias no primeiro ano de trabalho efectivo (dias de calendário). */
    public static final int DIAS_FERIAS_PRIMEIRO_ANO = 12;
    /** Art. 108.º: férias nos anos seguintes (dias de calendário). */
    public static final int DIAS_FERIAS_ANUAIS = 30;
    /** Art. 109.º: férias podem ser fraccionadas por acordo, em períodos nunca inferiores a 6 dias. */
    public static final int MIN_DIAS_FRACAO_FERIAS = 6;
    /** Limite de sanidade para horas mensais de um contratado à hora (48h/semana, art. 93.º, mais trabalho extraordinário). */
    public static final int MAX_HORAS_MES = 250;

    /** Art. 105.º: feriados nacionais fixos. Se cair a um domingo, o descanso passa para a segunda-feira. */
    private static final int[][] FERIADOS = {
            {1, 1},   // Ano Novo
            {2, 3},   // Dia dos Heróis Moçambicanos
            {4, 7},   // Dia da Mulher Moçambicana
            {5, 1},   // Dia Internacional dos Trabalhadores
            {6, 25},  // Dia da Independência Nacional
            {9, 7},   // Dia da Vitória
            {9, 25},  // Dia das Forças Armadas de Libertação Nacional
            {10, 4},  // Dia da Paz e Reconciliação
            {12, 25}  // Dia da Família
    };

    /** Este dia é dia feriado (já com a regra do domingo)? Feriados decretados pontualmente não estão incluídos. */
    public static boolean ehFeriado(LocalDate d) {
        for (int[] f : FERIADOS) {
            LocalDate h = LocalDate.of(d.getYear(), Month.of(f[0]), f[1]);
            LocalDate observado = h.getDayOfWeek() == java.time.DayOfWeek.SUNDAY ? h.plusDays(1) : h;
            if (observado.equals(d)) return true;
        }
        return false;
    }

    /** Arredonda a 2 casas decimais (meio para cima). */
    public static double arredondar(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
