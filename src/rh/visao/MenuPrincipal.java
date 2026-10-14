package rh.visao;

import java.awt.*;
import javax.swing.*;
import rh.modelo.Permissao;
import rh.modelo.Utilizador;

/** TELA 1 - Menu principal. Só mostra o que o perfil do utilizador pode usar. */
public class MenuPrincipal extends JFrame {

    public MenuPrincipal(Navegador nav) {
        super("Gestão de Recursos Humanos");
        var sessao = nav.app().sessao;
        Utilizador u = sessao.utilizador();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { nav.sair(); }
        });

        JLabel titulo = new JLabel("Gestão de Recursos Humanos", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        JLabel quem = new JLabel(u.getNomeUtilizador() + "  |  " + u.getPerfil(), SwingConstants.CENTER);
        JPanel topo = new JPanel(new GridLayout(2, 1));
        topo.setBorder(BorderFactory.createEmptyBorder(16, 10, 8, 10));
        topo.add(titulo);
        topo.add(quem);

        JPanel botoes = new JPanel(new GridLayout(0, 1, 0, 10));
        botoes.setBorder(BorderFactory.createEmptyBorder(10, 40, 24, 40));
        if (sessao.tem(Permissao.FUNCIONARIOS_VER)) botoes.add(botao("Funcionários", () -> nav.abrir(Navegador.Tela.FUNCIONARIOS)));
        if (sessao.tem(Permissao.DEPARTAMENTOS_VER)) botoes.add(botao("Departamentos", () -> nav.abrir(Navegador.Tela.DEPARTAMENTOS)));
        botoes.add(botao(sessao.tem(Permissao.FERIAS_VER_TODAS) ? "Férias" : "As minhas férias",
                () -> nav.abrir(Navegador.Tela.FERIAS)));
        botoes.add(botao(sessao.tem(Permissao.FOLHA_VER_TODOS) ? "Folha de salários" : "O meu recibo",
                () -> nav.abrir(Navegador.Tela.FOLHA)));
        if (sessao.tem(Permissao.RELATORIOS_VER)) botoes.add(botao("Dashboard e relatórios", () -> nav.abrir(Navegador.Tela.DASHBOARD)));
        if (sessao.tem(Permissao.UTILIZADORES_GERIR)) botoes.add(botao("Utilizadores", () -> nav.abrir(Navegador.Tela.UTILIZADORES)));
        if (sessao.tem(Permissao.DEFINICOES_EDITAR)) botoes.add(botao("Definições da empresa", () -> nav.definicoes(this)));
        botoes.add(botao("Alterar palavra-passe", () -> nav.alterarPassword(this)));
        botoes.add(botao("Terminar sessão", nav::terminarSessao));
        botoes.add(botao("Sair", nav::sair));

        add(topo, BorderLayout.NORTH);
        add(botoes, BorderLayout.CENTER);
        pack();
        setSize(Math.max(getWidth(), 380), getHeight() + 20);
        setLocationRelativeTo(null);
    }

    private static JButton botao(String texto, Runnable acao) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> acao.run());
        return b;
    }
}
