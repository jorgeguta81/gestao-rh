package rh.servico;

import java.time.LocalDate;
import rh.modelo.*;

/**
 * Dados de um funcionário tal como vêm do formulário. valor1..3 dependem do tipo
 * (ver {@link TipoFuncionario#rotuloValor(int)}).
 */
public record DadosFuncionario(TipoFuncionario tipo, String nome, String bi, String nuit, LocalDate dataNascimento,
                               String email, String telefone, String cargo, int departamentoId, LocalDate dataAdmissao,
                               double valor1, double valor2, double valor3) {

    /** Extrai os dados de um funcionário existente (para preencher o formulário de edição). */
    public static DadosFuncionario de(Funcionario f) {
        double v1 = 0, v2 = 0, v3 = 0;
        if (f instanceof Gestor g) { v1 = g.getSalarioBase(); v2 = g.getSubsidios(); v3 = g.getBonus(); }
        else if (f instanceof FuncionarioEfetivo e) { v1 = e.getSalarioBase(); v2 = e.getSubsidios(); }
        else if (f instanceof FuncionarioContratado c) { v1 = c.getValorHora(); v2 = c.getHorasTrabalhadas(); }
        else if (f instanceof Estagiario s) { v1 = s.getBolsa(); }
        return new DadosFuncionario(TipoFuncionario.de(f), f.getNome(), f.getBi(), f.getNuit(), f.getDataNascimento(),
                f.getEmail(), f.getTelefone(), f.getCargo(), f.getDepartamentoId(), f.getDataAdmissao(), v1, v2, v3);
    }

    /** Cria o funcionário do tipo certo. */
    public Funcionario criar() {
        Funcionario f = novo();
        f.setNuit(nuit);
        f.setDataNascimento(dataNascimento);
        return f;
    }

    private Funcionario novo() {
        return switch (tipo) {
            case EFECTIVO -> new FuncionarioEfetivo(nome, bi, email, telefone, cargo, departamentoId, dataAdmissao, valor1, valor2);
            case GESTOR -> new Gestor(nome, bi, email, telefone, cargo, departamentoId, dataAdmissao, valor1, valor2, valor3);
            case CONTRATADO -> new FuncionarioContratado(nome, bi, email, telefone, cargo, departamentoId, dataAdmissao, valor1, (int) valor2);
            case ESTAGIARIO -> new Estagiario(nome, bi, email, telefone, cargo, departamentoId, dataAdmissao, valor1);
        };
    }

    /** Copia estes dados para um funcionário existente (o tipo não muda). */
    public void aplicarEm(Funcionario f) {
        f.setNome(nome); f.setBi(bi); f.setNuit(nuit); f.setDataNascimento(dataNascimento); f.setEmail(email); f.setTelefone(telefone);
        f.setCargo(cargo); f.setDepartamentoId(departamentoId); f.setDataAdmissao(dataAdmissao);
        if (f instanceof Gestor g) { g.setSalarioBase(valor1); g.setSubsidios(valor2); g.setBonus(valor3); }
        else if (f instanceof FuncionarioEfetivo e) { e.setSalarioBase(valor1); e.setSubsidios(valor2); }
        else if (f instanceof FuncionarioContratado c) { c.setValorHora(valor1); c.setHorasTrabalhadas((int) valor2); }
        else if (f instanceof Estagiario s) { s.setBolsa(valor1); }
    }
}
