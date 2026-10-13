package rh.visao;

import java.awt.Component;
import java.awt.GridBagLayout;
import javax.swing.*;
import rh.Aplicacao;
import rh.excecao.RhException;

/** Diálogo para mudar a palavra-passe (obrigatório no primeiro acesso). */
final class MudarPasswordDialogo {
    private MudarPasswordDialogo() {}

    /** @return true se a palavra-passe foi alterada; false se o utilizador cancelou. */
    static boolean mostrar(Component pai, Aplicacao app, boolean obrigatoria) {
        String aviso = obrigatoria ? "É o seu primeiro acesso (ou a palavra-passe foi redefinida): defina uma nova palavra-passe."
                                   : "Alterar palavra-passe";
        String erro = null;
        while (true) {
            JPasswordField actual = new JPasswordField(16), nova = new JPasswordField(16), conf = new JPasswordField(16);
            JPanel p = new JPanel(new GridBagLayout());
            Ui.linha(p, 0, new JLabel("Palavra-passe actual:"), actual);
            Ui.linha(p, 1, new JLabel("Nova palavra-passe:"), nova);
            Ui.linha(p, 2, new JLabel("Confirmar nova:"), conf);
            JPanel todo = new JPanel(new java.awt.BorderLayout());
            JLabel msg = new JLabel("<html>" + aviso + "<br>Mínimo 8 caracteres, com letras e números."
                    + (erro == null ? "" : "<br><font color='#b41e1e'>" + erro + "</font>") + "</html>");
            todo.add(msg, java.awt.BorderLayout.NORTH);
            todo.add(p, java.awt.BorderLayout.CENTER);

            int r = JOptionPane.showConfirmDialog(pai, todo, "Alterar palavra-passe",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) return false;
            try {
                String n = new String(nova.getPassword());
                if (!n.equals(new String(conf.getPassword())))
                    throw new IllegalArgumentException("A confirmação não coincide com a nova palavra-passe.");
                app.utilizadores.alterarPassword(new String(actual.getPassword()), n);
                return true;
            } catch (RhException | IllegalArgumentException ex) {
                erro = ex.getMessage();
            }
        }
    }
}
