package rh.modelo;

/** Tipos de funcionário e os rótulos dos campos de valores de cada um (usado pelos formulários). */
public enum TipoFuncionario {
    EFECTIVO("Efectivo", "Salário base", "Subsídios", null),
    GESTOR("Gestor", "Salário base", "Subsídios", "Bónus"),
    CONTRATADO("Contratado", "Valor/hora", "Horas trabalhadas", null),
    ESTAGIARIO("Estagiário", "Bolsa", null, null);

    private final String rotulo;
    private final String[] rotulosValores;

    TipoFuncionario(String rotulo, String v1, String v2, String v3) {
        this.rotulo = rotulo;
        this.rotulosValores = new String[]{v1, v2, v3};
    }

    /** Rótulo do campo de valor n (0, 1 ou 2); null se este tipo não usa esse campo. */
    public String rotuloValor(int n) { return rotulosValores[n]; }

    @Override public String toString() { return rotulo; }

    public static TipoFuncionario de(Funcionario f) {
        if (f instanceof Gestor) return GESTOR; // Gestor é um FuncionarioEfetivo: testar primeiro
        if (f instanceof FuncionarioEfetivo) return EFECTIVO;
        if (f instanceof FuncionarioContratado) return CONTRATADO;
        return ESTAGIARIO;
    }
}
