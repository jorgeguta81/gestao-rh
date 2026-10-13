package rh.modelo;

import java.time.LocalDate;

public class FuncionarioContratado extends Funcionario {
    private double valorHora;
    private int horasTrabalhadas;

    public FuncionarioContratado(String nome, String bi, String email, String telefone,
                                 String cargo, int departamentoId, LocalDate dataAdmissao,
                                 double valorHora, int horasTrabalhadas) {
        super(nome, bi, email, telefone, cargo, departamentoId, dataAdmissao);
        this.valorHora = valorHora;
        this.horasTrabalhadas = horasTrabalhadas;
    }

    public double getValorHora() { return valorHora; }
    public void setValorHora(double valorHora) { this.valorHora = valorHora; }
    public int getHorasTrabalhadas() { return horasTrabalhadas; }
    public void setHorasTrabalhadas(int horas) { this.horasTrabalhadas = horas; }

    @Override public String getTipo() { return "Contratado"; }

    @Override
    public double calcularSalarioBruto() { return valorHora * horasTrabalhadas; }
}
