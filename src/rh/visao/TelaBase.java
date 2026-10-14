package rh.visao;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import rh.Aplicacao;

/** Comportamento comum das telas: fechar só a janela, recarregar dados ao activar e botão "Voltar ao menu". */
public abstract class TelaBase extends JFrame {
    protected final Navegador nav;
    protected final Aplicacao app;
    private JPanel corpo; // área onde as telas colocam os seus componentes (por baixo do cabeçalho)

    protected TelaBase(String titulo, Navegador nav) {
        super(titulo);
        this.nav = nav;
        this.app = nav.app();
        var u = app.sessao.utilizador();
        String quem = u == null ? "" : u.getNomeUtilizador() + "  |  " + u.getPerfil();
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.putClientProperty(Tema.IGNORAR, true);
        raiz.add(Tema.banner(titulo, app.sessao.autenticado() ? app.definicoes.nomeEmpresa() : "", quem), BorderLayout.NORTH);
        corpo = new JPanel(new BorderLayout(0, 8));
        corpo.putClientProperty(Tema.IGNORAR, true);
        corpo.setBackground(Tema.FUNDO);
        corpo.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        raiz.add(corpo, BorderLayout.CENTER);
        setContentPane(raiz);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowActivated(WindowEvent e) { atualizar(); }
        });
    }

    /** Tudo o que a tela adiciona (add(...)) vai para a área por baixo do cabeçalho. */
    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        if (corpo != null && isRootPaneCheckingEnabled()) corpo.add(comp, constraints, index);
        else super.addImpl(comp, constraints, index);
    }

    /** Volta a ler os dados dos controladores e redesenha a tela. */
    protected abstract void atualizar();

    /** Botão que fecha esta tela e regressa ao menu. */
    protected JButton botaoVoltar() {
        JButton b = new JButton("Voltar ao menu");
        b.addActionListener(e -> nav.voltarAoMenu(this));
        return b;
    }

    /** Rodapé simples, alinhado à direita, com o botão de voltar. */
    protected JPanel rodapeVoltar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.add(botaoVoltar());
        return p;
    }

    protected void dimensionar(int largura, int altura) {
        setSize(largura, altura + 56); // + altura do cabeçalho
        setLocationRelativeTo(null);
    }
}
