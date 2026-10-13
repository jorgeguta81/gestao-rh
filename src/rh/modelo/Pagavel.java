package rh.modelo;

/** Interface: quem recebe salário. Cada tipo de funcionário calcula o bruto à sua maneira. */
public interface Pagavel {
    double TAXA_INSS = 0.03; // 3% a cargo do trabalhador (confirme a taxa em vigor)

    double calcularSalarioBruto();

    default double calcularInss() {
        return calcularSalarioBruto() * TAXA_INSS;
    }

    default double calcularSalarioLiquido() {
        return calcularSalarioBruto() - calcularInss();
    }
}
