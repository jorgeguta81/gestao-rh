package rh.controlador;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import rh.excecao.PersistenciaException;
import rh.modelo.Permissao;
import rh.servico.RelatorioServico;
import rh.servico.RelatorioServico.Painel;
import rh.servico.RelatorioServico.Relatorio;

/** Controlador (MVC): dashboard e exportação de relatórios (RH e Administrador). */
public class RelatorioControlador {
    private static final String BOM_UTF8 = "\uFEFF"; // para o Excel reconhecer os acentos

    private final RelatorioServico servico;
    private final Sessao sessao;

    public RelatorioControlador(RelatorioServico servico, Sessao sessao) {
        this.servico = servico;
        this.sessao = sessao;
    }

    public Painel painel() {
        sessao.exigir(Permissao.RELATORIOS_VER);
        return servico.painel(LocalDate.now());
    }

    /** Grava o relatório em CSV (UTF-8 com BOM, separador ';'). A exportação fica registada na auditoria. */
    public void exportar(Relatorio relatorio, Path destino) {
        sessao.exigir(Permissao.RELATORIOS_VER);
        try {
            Files.writeString(destino, BOM_UTF8 + servico.csv(relatorio), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível gravar o ficheiro \"" + destino + "\".", e);
        }
        sessao.auditar("RELATORIO_EXPORTADO tipo=" + relatorio.name());
    }
}
