package rh;

import javax.swing.SwingUtilities;
import rh.visao.MenuPrincipal;

/** Ponto de entrada da aplicação (NetBeans: main.class=rh.Main). */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuPrincipal(new Aplicacao()).setVisible(true));
    }
}
