package rh.servico;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import rh.config.SectorActividade;
import rh.excecao.PersistenciaException;

/** Definições da empresa (nome e sector de actividade), guardadas em definicoes.properties. */
public class DefinicoesServico {
    private final Path ficheiro;
    private final Properties props = new Properties();

    public DefinicoesServico(Path ficheiro) {
        this.ficheiro = ficheiro;
        if (Files.exists(ficheiro)) {
            try (InputStream in = Files.newInputStream(ficheiro)) {
                props.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new PersistenciaException("Não foi possível ler as definições (" + ficheiro + ").", e);
            }
        }
    }

    public String nomeEmpresa() { return props.getProperty("empresa.nome", ""); }

    public SectorActividade sector() {
        try {
            return SectorActividade.valueOf(props.getProperty("empresa.sector", SectorActividade.PADRAO.name()));
        } catch (IllegalArgumentException e) {
            return SectorActividade.PADRAO;
        }
    }

    public double salarioMinimo() { return sector().salarioMinimo(); }

    public void guardar(String nomeEmpresa, SectorActividade sector) {
        props.setProperty("empresa.nome", nomeEmpresa == null ? "" : nomeEmpresa.trim());
        props.setProperty("empresa.sector", sector.name());
        try {
            Files.createDirectories(ficheiro.toAbsolutePath().getParent());
            try (OutputStream out = Files.newOutputStream(ficheiro)) {
                props.store(new java.io.OutputStreamWriter(out, java.nio.charset.StandardCharsets.UTF_8), "Definicoes da empresa");
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível gravar as definições.", e);
        }
    }
}
