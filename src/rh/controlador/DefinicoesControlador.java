package rh.controlador;

import rh.config.SectorActividade;
import rh.modelo.Permissao;
import rh.servico.DefinicoesServico;

/** Controlador (MVC): definições da empresa (só o Administrador altera). */
public class DefinicoesControlador {
    private final DefinicoesServico servico;
    private final Sessao sessao;

    public DefinicoesControlador(DefinicoesServico servico, Sessao sessao) {
        this.servico = servico;
        this.sessao = sessao;
    }

    public String nomeEmpresa() { sessao.exigirAutenticado(); return servico.nomeEmpresa(); }
    public SectorActividade sector() { sessao.exigirAutenticado(); return servico.sector(); }

    public void guardar(String nomeEmpresa, SectorActividade sector) {
        sessao.exigir(Permissao.DEFINICOES_EDITAR);
        servico.guardar(nomeEmpresa, sector);
    }
}
