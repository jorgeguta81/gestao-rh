package rh.modelo;

import java.time.LocalDate;

/** Herança em 2 níveis: Gestor -> FuncionarioEfetivo -> Funcionario -> Pessoa. */
public class Gestor extends FuncionarioEfetivo {
    private double bonus;

    public Gestor(String nome, String bi, String email, String telefone,
                  String cargo, int departamentoId, LocalDate dataAdmissao,
                  double salarioBase, double subsidios, double bonus) {
        super(nome, bi, email, telefone, cargo, departamentoId, dataAdmissao, salarioBase, subsidios);
        this.bonus = bonus;
    }

    public double getBonus() { return bonus; }
    public void setBonus(double bonus) { this.bonus = bonus; }

    @Override public String getTipo() { return "Gestor"; }

    @Override
    public double calcularSalarioBruto() { return super.calcularSalarioBruto() + bonus; }
}
