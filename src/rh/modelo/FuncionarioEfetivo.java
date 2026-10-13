package rh.modelo;

import java.time.LocalDate;

public class FuncionarioEfetivo extends Funcionario {
    private double salarioBase;
    private double subsidios;

    public FuncionarioEfetivo(String nome, String bi, String email, String telefone,
                              String cargo, int departamentoId, LocalDate dataAdmissao,
                              double salarioBase, double subsidios) {
        super(nome, bi, email, telefone, cargo, departamentoId, dataAdmissao);
        this.salarioBase = salarioBase;
        this.subsidios = subsidios;
    }

    public double getSalarioBase() { return salarioBase; }
    public void setSalarioBase(double salarioBase) { this.salarioBase = salarioBase; }
    public double getSubsidios() { return subsidios; }
    public void setSubsidios(double subsidios) { this.subsidios = subsidios; }

    @Override public String getTipo() { return "Efectivo"; }

    @Override
    public double calcularSalarioBruto() { return salarioBase + subsidios; }
}
