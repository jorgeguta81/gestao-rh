package rh;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import rh.excecao.PersistenciaException;
import rh.visao.Navegador;

/** Ponto de entrada da aplicação (NetBeans: main.class=rh.Main). */
public class Main {
    public static void main(String[] args) {
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
