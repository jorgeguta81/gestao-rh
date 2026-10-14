package rh.visao;

import java.awt.*;
import javax.swing.*;
import rh.excecao.RhException;

/** TELA 0 - Login: painel de marca à esquerda, formulário à direita. */
public class LoginFrame extends JFrame {
    private final JTextField tfUtilizador = new JTextField(18);
    private final JPasswordField pfPassword = new JPasswordField(18);
    private final JLabel lbErro = new JLabel(" ");

    public LoginFrame(Navegador nav) {
        super("Gestão de RH - Entrar");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { nav.sair(); }
        });

        // ---- esquerda: marca
        JPanel marca = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, Tema.AZUL, 0, getHeight(), Tema.AZUL_MEDIO));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0xF2B705));
                g2.fillRect(getWidth() - 4, 0, 4, getHeight());
                g2.dispose();
            }
        };
        marca.putClientProperty(Tema.IGNORAR, true);
        marca.setPreferredSize(new Dimension(330, 420));
        JPanel texto = new JPanel(new GridLayout(0, 1, 0, 6));
        texto.setOpaque(false);
        texto.putClientProperty(Tema.IGNORAR, true);
        JLabel t1 = new JLabel("Gestão de");
        JLabel t2 = new JLabel("Recursos Humanos");
        t1.setFont(Tema.letra(Font.PLAIN, 24)); t1.setForeground(new Color(0xC9D8EE));
        t2.setFont(Tema.letra(Font.BOLD, 28)); t2.setForeground(Color.WHITE);
        JLabel t3 = new JLabel("<html>Para empresas privadas<br>moçambicanas</html>");
        t3.setFont(Tema.letra(Font.PLAIN, 14)); t3.setForeground(new Color(0xE6EEF9));
        JLabel t4 = new JLabel("<html><br>Lei do Trabalho 13/2023 &middot; INSS &middot; IRPS</html>");
        t4.setFont(Tema.letra(Font.PLAIN, 12)); t4.setForeground(new Color(0xF2B705));
        texto.add(t1); texto.add(t2); texto.add(t3); texto.add(t4);
        marca.add(texto);

        // ---- direita: formulário
        JPanel direita = new JPanel(new GridBagLayout());
        direita.putClientProperty(Tema.IGNORAR, true);
        direita.setBackground(Tema.CARTAO);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.insets = new Insets(3, 0, 3, 0);
        JLabel titulo = new JLabel("Entrar");
        titulo.setFont(Tema.letra(Font.BOLD, 24)); titulo.setForeground(Tema.AZUL);
        JLabel sub = new JLabel("Introduza as suas credenciais");
        sub.setForeground(Tema.SUAVE);
        c.gridy = 0; form.add(titulo, c);
        c.gridy = 1; c.insets = new Insets(0, 0, 18, 0); form.add(sub, c);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = 2; form.add(rotulo("Utilizador"), c);
        c.gridy = 3; form.add(tfUtilizador, c);
        c.gridy = 4; c.insets = new Insets(10, 0, 3, 0); form.add(rotulo("Palavra-passe"), c);
        c.gridy = 5; c.insets = new Insets(3, 0, 3, 0); form.add(pfPassword, c);
        lbErro.setForeground(Tema.VERMELHO);
        c.gridy = 6; c.insets = new Insets(8, 0, 8, 0); form.add(lbErro, c);

        JButton bEntrar = new JButton("Entrar");
        bEntrar.addActionListener(e -> entrar(nav));
        getRootPane().setDefaultButton(bEntrar);
        JButton bSair = new JButton("Sair");
        bSair.addActionListener(e -> nav.sair());
        JPanel botoes = new JPanel(new GridLayout(1, 2, 10, 0));
        botoes.setOpaque(false);
        botoes.add(bEntrar);
        botoes.add(bSair);
        c.gridy = 7; c.insets = new Insets(4, 0, 0, 0); form.add(botoes, c);
        direita.add(form);
        direita.setBorder(BorderFactory.createEmptyBorder(0, 36, 0, 36));

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.putClientProperty(Tema.IGNORAR, true);
        raiz.add(marca, BorderLayout.WEST);
        raiz.add(direita, BorderLayout.CENTER);
        setContentPane(raiz);
        setSize(760, 420);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private static JLabel rotulo(String t) {
        JLabel l = new JLabel(t);
        l.setFont(Tema.letra(Font.BOLD, 12));
        l.setForeground(Tema.TEXTO);
        return l;
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
            lbErro.setText("<html>" + ex.getMessage() + "</html>");
            pfPassword.requestFocus();
        }
    }
}
