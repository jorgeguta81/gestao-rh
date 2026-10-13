package rh.modelo;

import static rh.config.ParametrosLegais.*;

/**
 * Quem recebe salário. Cada tipo de funcionário calcula o bruto à sua maneira;
 * os descontos seguem a lei (ver rh.config.ParametrosLegais).
 */
public interface Pagavel {

    double calcularSalarioBruto();

    /** INSS a cargo do trabalhador (3%). */
    default double calcularInss() {
        return arredondar(calcularSalarioBruto() * INSS_TRABALHADOR);
    }

    /** Contribuição patronal para o INSS (4%): custo da empresa, não é descontada ao trabalhador. */
    default double calcularInssPatronal() {
        return arredondar(calcularSalarioBruto() * INSS_EMPREGADOR);
    }

    /** IRPS retido na fonte (tabela mensal, 0 dependentes). */
    default double calcularIrps() {
        double base = calcularSalarioBruto() - (INSS_REDUZ_BASE_IRPS ? calcularInss() : 0);
        return irps(base);
    }

    default double calcularSalarioLiquido() {
        return arredondar(calcularSalarioBruto() - calcularInss() - calcularIrps());
    }
}
