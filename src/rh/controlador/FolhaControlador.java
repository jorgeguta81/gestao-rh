package rh.controlador;

import java.util.List;
import rh.excecao.AcessoNegadoException;
import rh.modelo.Permissao;
import rh.servico.FolhaSalarioServico;
import rh.servico.FolhaSalarioServico.LinhaFolha;

/** Controlador (MVC): folha de salários. */
public class FolhaControlador {
    private final FolhaSalarioServico servico;
    private final Sessao sessao;

    public FolhaControlador(FolhaSalarioServico servico, Sessao sessao) {
        this.servico = servico;
        this.sessao = sessao;
    }

    /** RH/Admin: folha completa. Funcionário: só o seu recibo. */
    public List<LinhaFolha> linhas() {
        sessao.exigirAutenticado();
        if (sessao.tem(Permissao.FOLHA_VER_TODOS)) return servico.calcular();
        if (sessao.tem(Permissao.RECIBO_PROPRIO))
            return servico.calcular().stream().filter(l -> l.id() == sessao.funcionarioId()).toList();
        throw new AcessoNegadoException("O seu perfil não tem acesso à folha de salários.");
    }
}
