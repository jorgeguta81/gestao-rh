package rh;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import rh.excecao.PersistenciaException;
import rh.util.Registo;
import rh.visao.Navegador;

/** Ponto de entrada da aplicação (NetBeans: main.class=rh.Main). */
public class Main {
    public static void main(String[] args) {
        // Rede de segurança: qualquer erro inesperado é registado e o utilizador vê uma mensagem simples.
        Thread.setDefaultUncaughtExceptionHandler((thread, erro) -> {
            Registo.erro("Erro não tratado na thread " + thread.getName(), erro);
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Ocorreu um erro inesperado.\nDetalhes técnicos em: " + Registo.ficheiroDeErros(),
                    "Erro inesperado", JOptionPane.ERROR_MESSAGE));
        });
        SwingUtilities.invokeLater(() -> {
            try {
                new Navegador(new Aplicacao()).iniciar();
            } catch (PersistenciaException e) {
                JOptionPane.showMessageDialog(null, e.getMessage() + "\nPasta de dados: " + Aplicacao.pastaDados().toAbsolutePath(),
                        "Erro ao abrir os dados", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
