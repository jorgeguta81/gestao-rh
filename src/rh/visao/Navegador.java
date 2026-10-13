package rh.visao;

import java.util.EnumMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import rh.Aplicacao;

/**
 * Gere a navegação entre telas: cada tela tem no máximo uma janela aberta
 * (se já existe, é trazida para a frente em vez de abrir outra).
 */
public final class Navegador {

    /** Todas as telas da aplicação. */
    public enum Tela { MENU, FUNCIONARIOS, DEPARTAMENTOS, FERIAS, FOLHA }

    private final Aplicacao app;
    private final Map<Tela, JFrame> abertas = new EnumMap<>(Tela.class);

    public Navegador(Aplicacao app) { this.app = app; }

    public Aplicacao app() { return app; }

    /** Arranque: mostra o menu principal. */
    public void iniciar() { abrir(Tela.MENU); }

    public void abrir(Tela tela) {
        JFrame f = abertas.get(tela);
        if (f == null || !f.isDisplayable()) {
            f = criar(tela);
            abertas.put(tela, f);
        }
        f.setVisible(true);
        f.setExtendedState(JFrame.NORMAL);
        f.toFront();
        f.requestFocus();
    }

    /** Fecha a tela actual e volta ao menu principal. */
    public void voltarAoMenu(JFrame actual) {
        if (actual != null) actual.dispose();
        abrir(Tela.MENU);
    }

    /** Pede confirmação e termina a aplicação. */
    public void sair() {
        JFrame menu = abertas.get(Tela.MENU);
        int r = JOptionPane.showConfirmDialog(menu, "Deseja sair da aplicação?", "Sair",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;
        for (JFrame f : abertas.values()) f.dispose();
        System.exit(0);
    }

    private JFrame criar(Tela tela) {
        return switch (tela) {
            case MENU -> new MenuPrincipal(this);
            case FUNCIONARIOS -> new FuncionariosFrame(this);
            case DEPARTAMENTOS -> new DepartamentosFrame(this);
            case FERIAS -> new FeriasFrame(this);
            case FOLHA -> new FolhaFrame(this);
        };
    }
}
