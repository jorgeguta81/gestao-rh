package rh.visao;

import java.awt.*;
import javax.swing.*;
import rh.excecao.RhException;

/** TELA 0 - Login. */
public class LoginFrame extends JFrame {
    private final JTextField tfUtilizador = new JTextField(16);
    private final JPasswordField pfPassword = new JPasswordField(16);
    private final JLabel lbErro = new JLabel(" ", SwingConstants.CENTER);

    public LoginFrame(Navegador nav) {
        super("Gestão de RH - Entrar");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { nav.sair(); }
        });

        JLabel titulo = new JLabel("Gestão de Recursos Humanos", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        titulo.setBorder(BorderFactory.createEmptyBorder(16, 10, 4, 10));
        lbErro.setForeground(new Color(180, 30, 30));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));
        Ui.linha(form, 0, new JLabel("Utilizador:"), tfUtilizador);
        Ui.linha(form, 1, new JLabel("Palavra-passe:"), pfPassword);

        JButton bEntrar = new JButton("Entrar");
        bEntrar.addActionListener(e -> entrar(nav));
        getRootPane().setDefaultButton(bEntrar);
        JButton bSair = new JButton("Sair");
        bSair.addActionListener(e -> nav.sair());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botoes.add(bEntrar);
        botoes.add(bSair);

        JPanel sul = new JPanel(new BorderLayout());
        sul.add(lbErro, BorderLayout.NORTH);
        sul.add(botoes, BorderLayout.CENTER);

        add(titulo, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(sul, BorderLayout.SOUTH);
        setSize(400, 260);
        setLocationRelativeTo(null);
    }

    private void entrar(Navegador nav) {
        String pwd = new String(pfPassword.getPassword());
        pfPassword.setText("");
        try {
            nav.app().utilizadores.login(tfUtilizador.getText(), pwd);
            lbErro.setText(" ");
            tfUtilizador.setText("");
            nav.aposLogin(this);
        } catch (RhException ex) {
            lbErro.setText(ex.getMessage());
            pfPassword.requestFocus();
        }
    }
}
