package rh.controlador;

import java.util.List;
import rh.excecao.AcessoNegadoException;
import rh.modelo.Permissao;
import rh.servico.FolhaSalarioServico;
import rh.servico.RelatorioServico;
import rh.servico.FolhaSalarioServico.LinhaFolha;

/** Controlador (MVC): folha de salários. */
public class FolhaControlador {
    private final FolhaSalarioServico servico;
    private final Sessao sessao;
    private final RelatorioServico relatorios;

    public FolhaControlador(FolhaSalarioServico servico, RelatorioServico relatorios, Sessao sessao) {
        this.servico = servico;
        this.relatorios = relatorios;
        this.sessao = sessao;
    }

    /** Recibo de vencimento em texto: RH/Admin de qualquer funcionário; cada um só o seu. */
    public String recibo(int funcionarioId, java.time.YearMonth mes) {
        sessao.exigirAutenticado();
        boolean proprio = sessao.tem(Permissao.RECIBO_PROPRIO) && funcionarioId == sessao.funcionarioId();
        if (!proprio && !sessao.tem(Permissao.FOLHA_VER_TODOS))
            throw new AcessoNegadoException("Só pode consultar o seu próprio recibo.");
        sessao.auditar("RECIBO_CONSULTADO funcionario=" + funcionarioId);
        return relatorios.recibo(funcionarioId, mes);
    }

    /** RH/Admin: folha completa. Funcionário: só o seu recibo. */
    public List<LinhaFolha> linhas() { return linhas("", null); }

    /** Folha filtrada por nome e/ou tipo. RH/Admin: completa. Funcionário: só o seu recibo. */
    public List<LinhaFolha> linhas(String texto, rh.modelo.TipoFuncionario tipo) {
        sessao.exigirAutenticado();
        if (sessao.tem(Permissao.FOLHA_VER_TODOS)) return servico.calcular(texto, tipo);
        if (sessao.tem(Permissao.RECIBO_PROPRIO))
            return servico.calcular(texto, tipo).stream().filter(l -> l.id() == sessao.funcionarioId()).toList();
        throw new AcessoNegadoException("O seu perfil não tem acesso à folha de salários.");
    }
}
