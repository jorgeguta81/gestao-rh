package rh;

import javax.swing.SwingUtilities;
import rh.visao.Navegador;

/** Ponto de entrada da aplicação (NetBeans: main.class=rh.Main). */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Navegador(new Aplicacao()).iniciar());
    }
}
