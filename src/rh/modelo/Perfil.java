package rh.modelo;

import java.util.EnumSet;
import java.util.Set;
import static rh.modelo.Permissao.*;

/** Os três perfis da aplicação e o que cada um pode fazer. */
public enum Perfil {
    ADMINISTRADOR("Administrador", EnumSet.allOf(Permissao.class)),

    GESTOR_RH("Gestor de RH", EnumSet.of(
            FUNCIONARIOS_VER, FUNCIONARIOS_EDITAR,
            DEPARTAMENTOS_VER, DEPARTAMENTOS_EDITAR,
            FERIAS_SOLICITAR, FERIAS_VER_TODAS, FERIAS_DECIDIR,
            FOLHA_VER_TODOS, RECIBO_PROPRIO, RELATORIOS_VER)),

    FUNCIONARIO("Funcionário", EnumSet.of(FERIAS_SOLICITAR, RECIBO_PROPRIO));

    private final String rotulo;
    private final Set<Permissao> permissoes;

    Perfil(String rotulo, Set<Permissao> permissoes) {
        this.rotulo = rotulo;
        this.permissoes = permissoes;
    }

    public boolean permite(Permissao p) { return permissoes.contains(p); }

    @Override public String toString() { return rotulo; }
}
