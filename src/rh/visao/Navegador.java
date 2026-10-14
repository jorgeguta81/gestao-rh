package rh.visao;

import java.util.EnumMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import rh.Aplicacao;
import rh.modelo.Utilizador;

/**
 * Gere a navegação entre telas: cada tela tem no máximo uma janela aberta
 * (se já existe, é trazida para a frente em vez de abrir outra).
 * Fluxo: LOGIN -> (mudança obrigatória de palavra-passe) -> MENU -> telas -> terminar sessão -> LOGIN.
 */
public final class Navegador {

    /** Todas as telas da aplicação. */
    public enum Tela { LOGIN, MENU, FUNCIONARIOS, DEPARTAMENTOS, FERIAS, FOLHA, UTILIZADORES, DASHBOARD }

    private final Aplicacao app;
    private final Map<Tela, JFrame> abertas = new EnumMap<>(Tela.class);

    public Navegador(Aplicacao app) {
        this.app = app;
        Tema.aplicar(); // tem de ser antes de criar qualquer janela
    }

    public Aplicacao app() { return app; }

    /** Arranque: mostra o login. */
    public void iniciar() { abrir(Tela.LOGIN); }

    public void abrir(Tela tela) {
        JFrame f = abertas.get(tela);
        if (f == null || !f.isDisplayable()) {
            f = criar(tela);
            Tema.estilizar(f.getContentPane());
            abertas.put(tela, f);
        }
        f.setVisible(true);
        f.setExtendedState(JFrame.NORMAL);
        f.toFront();
        f.requestFocus();
    }

    /** Chamado pelo login depois de credenciais válidas. */
    void aposLogin(JFrame login) {
        Utilizador u = app.sessao.utilizador();
        if (u.isMudarPassword() && !MudarPasswordDialogo.mostrar(login, app, true)) {
            app.utilizadores.terminarSessao(); // recusou mudar: não entra
            return;
        }
        login.dispose();
        abrir(Tela.MENU);
    }

    /** Fecha todas as janelas, termina a sessão e volta ao login. */
    public void terminarSessao() {
        app.utilizadores.terminarSessao();
        for (JFrame f : abertas.values()) f.dispose();
        abertas.clear();
        abrir(Tela.LOGIN);
    }

    /** Fecha a tela actual e volta ao menu principal. */
    public void voltarAoMenu(JFrame actual) {
        if (actual != null) actual.dispose();
        abrir(Tela.MENU);
    }

    /** Pede confirmação e termina a aplicação. */
    public void sair() {
        JFrame pai = abertas.get(Tela.MENU) != null ? abertas.get(Tela.MENU) : abertas.get(Tela.LOGIN);
        int r = JOptionPane.showConfirmDialog(pai, "Deseja sair da aplicação?", "Sair",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;
        for (JFrame f : abertas.values()) f.dispose();
        System.exit(0);
    }

    /** Altera a palavra-passe do utilizador actual (a partir do menu). */
    void alterarPassword(JFrame pai) {
        if (MudarPasswordDialogo.mostrar(pai, app, false))
            JOptionPane.showMessageDialog(pai, "Palavra-passe alterada.");
    }

    /** Definições da empresa (só Administrador). */
    void definicoes(JFrame pai) { DefinicoesDialogo.mostrar(pai, app); }

    private JFrame criar(Tela tela) {
        return switch (tela) {
            case LOGIN -> new LoginFrame(this);
            case MENU -> new MenuPrincipal(this);
            case FUNCIONARIOS -> new FuncionariosFrame(this);
            case DEPARTAMENTOS -> new DepartamentosFrame(this);
            case FERIAS -> new FeriasFrame(this);
            case FOLHA -> new FolhaFrame(this);
            case UTILIZADORES -> new UtilizadoresFrame(this);
            case DASHBOARD -> new DashboardFrame(this);
        };
    }
}
