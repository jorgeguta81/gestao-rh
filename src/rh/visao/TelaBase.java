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

    protected TelaBase(String titulo, Navegador nav) {
        super(titulo);
        this.nav = nav;
        this.app = nav.app();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowActivated(WindowEvent e) { atualizar(); }
        });
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
        setSize(largura, altura);
        setLocationRelativeTo(null);
    }
}
