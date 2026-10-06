package rh.ui;

import java.awt.*;
import javax.swing.*;

/** TELA 1 - Menu principal. Execute ESTA classe para abrir a aplicação. */
public class MenuPrincipal extends JFrame {

    public MenuPrincipal() {
        super("Gestão de Recursos Humanos");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel titulo = new JLabel("Gestão de Recursos Humanos", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        titulo.setBorder(BorderFactory.createEmptyBorder(16, 10, 8, 10));

        JButton bFunc = new JButton("Funcionários");
        JButton bFerias = new JButton("Férias");
        JButton bFolha = new JButton("Folha de salários");
        JButton bSair = new JButton("Sair");

        bFunc.addActionListener(e -> new FuncionariosFrame().setVisible(true));
        bFerias.addActionListener(e -> new FeriasFrame().setVisible(true));
        bFolha.addActionListener(e -> new FolhaFrame().setVisible(true));
        bSair.addActionListener(e -> System.exit(0));

        JPanel botoes = new JPanel(new GridLayout(4, 1, 0, 10));
        botoes.setBorder(BorderFactory.createEmptyBorder(10, 40, 24, 40));
        botoes.add(bFunc);
        botoes.add(bFerias);
        botoes.add(bFolha);
        botoes.add(bSair);

        add(titulo, BorderLayout.NORTH);
        add(botoes, BorderLayout.CENTER);
        setSize(360, 330);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuPrincipal().setVisible(true));
    }
}
