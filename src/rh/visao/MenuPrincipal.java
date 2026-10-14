package rh.visao;

import java.awt.*;
import javax.swing.*;
import rh.modelo.Permissao;
import rh.modelo.Utilizador;

/** TELA 1 - Menu principal: cartões com o que o perfil do utilizador pode usar. */
public class MenuPrincipal extends JFrame {

    public MenuPrincipal(Navegador nav) {
        super("Gestão de Recursos Humanos");
        var sessao = nav.app().sessao;
        Utilizador u = sessao.utilizador();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { nav.sair(); }
        });

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.putClientProperty(Tema.IGNORAR, true);
        raiz.setBackground(Tema.FUNDO);
        String empresa = nav.app().definicoes.nomeEmpresa();
        raiz.add(Tema.banner("Gestão de Recursos Humanos", empresa.isBlank() ? "Empresas privadas moçambicanas" : empresa,
                u.getNomeUtilizador() + "  |  " + u.getPerfil()), BorderLayout.NORTH);

        JPanel cartoes = new JPanel(new GridLayout(0, 3, 16, 16));
        cartoes.putClientProperty(Tema.IGNORAR, true);
        cartoes.setOpaque(false);
        cartoes.setBorder(BorderFactory.createEmptyBorder(24, 28, 12, 28));
        if (sessao.tem(Permissao.FUNCIONARIOS_VER))
            cartoes.add(Tema.cartaoMenu("Funcionários", "Admissões, fichas e estado", new Color(0x2F6DB5), () -> nav.abrir(Navegador.Tela.FUNCIONARIOS)));
        if (sessao.tem(Permissao.DEPARTAMENTOS_VER))
            cartoes.add(Tema.cartaoMenu("Departamentos", "Estrutura da empresa", new Color(0x6C5CE7), () -> nav.abrir(Navegador.Tela.DEPARTAMENTOS)));
        cartoes.add(Tema.cartaoMenu(sessao.tem(Permissao.FERIAS_VER_TODAS) ? "Férias" : "As minhas férias",
                "Pedidos, saldo e decisões", new Color(0x1E8E5A), () -> nav.abrir(Navegador.Tela.FERIAS)));
        cartoes.add(Tema.cartaoMenu(sessao.tem(Permissao.FOLHA_VER_TODOS) ? "Folha de salários" : "O meu recibo",
                "Salário, INSS e IRPS", new Color(0xE67E22), () -> nav.abrir(Navegador.Tela.FOLHA)));
        if (sessao.tem(Permissao.RELATORIOS_VER))
            cartoes.add(Tema.cartaoMenu("Dashboard", "Indicadores e relatórios", new Color(0xC0392B), () -> nav.abrir(Navegador.Tela.DASHBOARD)));
        if (sessao.tem(Permissao.UTILIZADORES_GERIR))
            cartoes.add(Tema.cartaoMenu("Utilizadores", "Contas e perfis de acesso", new Color(0x16A0AD), () -> nav.abrir(Navegador.Tela.UTILIZADORES)));
        if (sessao.tem(Permissao.DEFINICOES_EDITAR))
            cartoes.add(Tema.cartaoMenu("Definições", "Empresa e sector", new Color(0x7F8C8D), () -> nav.definicoes(this)));

        JPanel centro = new JPanel(new BorderLayout());
        centro.putClientProperty(Tema.IGNORAR, true);
        centro.setOpaque(false);
        centro.add(cartoes, BorderLayout.NORTH);
        raiz.add(centro, BorderLayout.CENTER);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rodape.setBorder(BorderFactory.createEmptyBorder(0, 20, 8, 20));
        JButton bPass = new JButton("Alterar palavra-passe");
        bPass.addActionListener(e -> nav.alterarPassword(this));
        JButton bSessao = new JButton("Terminar sessão");
        bSessao.addActionListener(e -> nav.terminarSessao());
        JButton bSair = new JButton("Sair");
        bSair.addActionListener(e -> nav.sair());
        rodape.add(bPass);
        rodape.add(bSessao);
        rodape.add(bSair);
        raiz.add(rodape, BorderLayout.SOUTH);

        setContentPane(raiz);
        setSize(920, 540);
        setLocationRelativeTo(null);
    }
}
