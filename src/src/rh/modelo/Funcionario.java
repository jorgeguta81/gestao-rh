package rh.modelo;

import java.time.LocalDate;

/** Funcionário: herda de Pessoa e implementa Pagavel. */
public abstract class Funcionario extends Pessoa implements Pagavel {
    private String cargo;
    private int departamentoId;
    private LocalDate dataAdmissao;
    private boolean ativo = true;

    protected Funcionario(String nome, String bi, String email, String telefone,
                          String cargo, int departamentoId, LocalDate dataAdmissao) {
        super(nome, bi, email, telefone);
        this.cargo = cargo;
        this.departamentoId = departamentoId;
        this.dataAdmissao = dataAdmissao;
    }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public int getDepartamentoId() { return departamentoId; }
    public void setDepartamentoId(int departamentoId) { this.departamentoId = departamentoId; }
    public LocalDate getDataAdmissao() { return dataAdmissao; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    @Override
    public String toString() {
        return super.toString() + " | " + cargo + " | Dep: " + departamentoId
                + " | " + (ativo ? "Activo" : "Inactivo");
    }
}
