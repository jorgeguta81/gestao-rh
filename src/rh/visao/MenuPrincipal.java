package rh.visao;

import java.awt.*;
import javax.swing.*;

/** TELA 1 - Menu principal. Abre as restantes telas através do Navegador. */
public class MenuPrincipal extends JFrame {

    private final Navegador nav;

    public MenuPrincipal(Navegador nav) {
        super("Gestão de Recursos Humanos");
        this.nav = nav;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { nav.sair(); }
        });

        JLabel titulo = new JLabel("Gestão de Recursos Humanos", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        titulo.setBorder(BorderFactory.createEmptyBorder(16, 10, 8, 10));

        JButton bFunc = new JButton("Funcionários");
        JButton bFerias = new JButton("Férias");
        JButton bDep = new JButton("Departamentos");
        JButton bFolha = new JButton("Folha de salários");
        JButton bSair = new JButton("Sair");

        bFunc.addActionListener(e -> nav.abrir(Navegador.Tela.FUNCIONARIOS));
        bFerias.addActionListener(e -> nav.abrir(Navegador.Tela.FERIAS));
        bFolha.addActionListener(e -> nav.abrir(Navegador.Tela.FOLHA));
        bDep.addActionListener(e -> nav.abrir(Navegador.Tela.DEPARTAMENTOS));
        bSair.addActionListener(e -> nav.sair());

        JPanel botoes = new JPanel(new GridLayout(5, 1, 0, 10));
        botoes.setBorder(BorderFactory.createEmptyBorder(10, 40, 24, 40));
        botoes.add(bFunc);
        botoes.add(bDep);
        botoes.add(bFerias);
        botoes.add(bFolha);
        botoes.add(bSair);

        add(titulo, BorderLayout.NORTH);
        add(botoes, BorderLayout.CENTER);
        setSize(360, 390);
        setLocationRelativeTo(null);
    }
}
