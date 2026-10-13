package rh.modelo;

import java.time.LocalDate;

public class Estagiario extends Funcionario {
    private double bolsa;

    public Estagiario(String nome, String bi, String email, String telefone,
                      String cargo, int departamentoId, LocalDate dataAdmissao, double bolsa) {
        super(nome, bi, email, telefone, cargo, departamentoId, dataAdmissao);
        this.bolsa = bolsa;
    }

    public double getBolsa() { return bolsa; }
    public void setBolsa(double bolsa) { this.bolsa = bolsa; }

    @Override public String getTipo() { return "Estagiario"; }

    @Override
    public double calcularSalarioBruto() { return bolsa; }

    /** Polimorfismo: estagiário não desconta INSS neste modelo. */
    @Override
    public double calcularInss() { return 0; }
}
