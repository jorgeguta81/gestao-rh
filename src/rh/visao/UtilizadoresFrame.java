package rh.visao;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import rh.excecao.RhException;
import rh.modelo.*;

/** TELA - Utilizadores (só Administrador): criar contas, mudar perfil, activar/desactivar, redefinir palavra-passe. */
public class UtilizadoresFrame extends TelaBase {
    /** Item da lista de funcionários (id 0 = nenhum). */
    private record Item(int id, String texto) { @Override public String toString() { return texto; } }

    private final DefaultTableModel modelo = Ui.modeloTabela("ID", "Utilizador", "Perfil", "Funcionário", "Estado", "Último acesso");
    private final JTable tabela = new JTable(modelo);
    private final JTextField tfNome = new JTextField(16);
    private final JPasswordField pfPassword = new JPasswordField(16);
    private final JComboBox<Perfil> cbPerfil = new JComboBox<>(Perfil.values());
    private final JComboBox<Item> cbFunc = new JComboBox<>();
    private final JCheckBox ckAtivo = new JCheckBox("Conta activa", true);
    private final JButton bGravar = new JButton("Criar"), bReset = new JButton("Redefinir palavra-passe"), bNovo = new JButton("Novo");
    private int editandoId = -1;
    private boolean aCarregar = false;

    public UtilizadoresFrame(Navegador nav) {
        super("Utilizadores", nav);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Conta de acesso"));
        Ui.linha(form, 0, new JLabel("Utilizador:"), tfNome);
        Ui.linha(form, 1, new JLabel("Palavra-passe inicial:"), pfPassword);
        Ui.linha(form, 2, new JLabel("Perfil:"), cbPerfil);
        Ui.linha(form, 3, new JLabel("Funcionário:"), cbFunc);
        Ui.linha(form, 4, new JLabel(""), ckAtivo);

        bNovo.addActionListener(e -> novo());
        bGravar.addActionListener(e -> gravar());
        bReset.addActionListener(e -> redefinir());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(bNovo); botoes.add(bGravar); botoes.add(bReset);

        JPanel esquerda = new JPanel(new BorderLayout());
        esquerda.add(form, BorderLayout.NORTH);
        esquerda.add(botoes, BorderLayout.CENTER);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Ui.larguras(tabela, 40, 100, 110, 160, 70, 130);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !aCarregar) aoSeleccionar();
        });

        add(esquerda, BorderLayout.WEST);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(rodapeVoltar(), BorderLayout.SOUTH);
        atualizar();
        novo();
        dimensionar(960, 420);
    }

    private int funcionarioEscolhido() { return ((Item) cbFunc.getSelectedItem()).id(); }

    private void gravar() {
        try {
            Perfil perfil = (Perfil) cbPerfil.getSelectedItem();
            if (editandoId < 0) {
                app.utilizadores.criar(tfNome.getText(), new String(pfPassword.getPassword()), perfil, funcionarioEscolhido());
                Ui.info(this, "Conta criada. O utilizador terá de mudar a palavra-passe no primeiro acesso.");
            } else {
                app.utilizadores.atualizar(editandoId, perfil, funcionarioEscolhido(), ckAtivo.isSelected());
            }
            novo();
            atualizar();
        } catch (RhException | IllegalArgumentException ex) {
            Ui.erro(this, ex.getMessage());
        }
    }

    private void redefinir() {
        try {
            if (!Ui.confirmar(this, "Redefinir a palavra-passe deste utilizador?")) return;
            String temp = app.utilizadores.redefinirPassword(editandoId);
            Ui.info(this, "Palavra-passe temporária: " + temp + "\nComunique-a ao utilizador; ele terá de a mudar no próximo acesso.");
            atualizar();
        } catch (RhException | IllegalArgumentException ex) {
            Ui.erro(this, ex.getMessage());
        }
    }

    private void novo() {
        editandoId = -1;
        tabela.clearSelection();
        tfNome.setText(""); pfPassword.setText("");
        tfNome.setEnabled(true); pfPassword.setEnabled(true);
        ckAtivo.setSelected(true); ckAtivo.setEnabled(false);
        cbPerfil.setSelectedItem(Perfil.FUNCIONARIO);
        cbFunc.setSelectedIndex(0);
        modoBotoes();
    }

    private void aoSeleccionar() {
        int id = Ui.idSeleccionado(tabela);
        if (id < 0) { editandoId = -1; modoBotoes(); return; }
        Utilizador u = app.utilizadores.listar().stream().filter(x -> x.getId() == id).findFirst().orElse(null);
        if (u == null) return;
        editandoId = id;
        tfNome.setText(u.getNomeUtilizador()); tfNome.setEnabled(false);
        pfPassword.setText(""); pfPassword.setEnabled(false);
        cbPerfil.setSelectedItem(u.getPerfil());
        for (int i = 0; i < cbFunc.getItemCount(); i++)
            if (cbFunc.getItemAt(i).id() == u.getFuncionarioId()) cbFunc.setSelectedIndex(i);
        ckAtivo.setSelected(u.isAtivo()); ckAtivo.setEnabled(true);
        modoBotoes();
    }

    private void modoBotoes() {
        bGravar.setText(editandoId >= 0 ? "Guardar" : "Criar");
        bReset.setEnabled(editandoId >= 0);
    }

    @Override
    protected void atualizar() {
        aCarregar = true;
        try {
            Object sel = cbFunc.getSelectedItem();
            cbFunc.removeAllItems();
            cbFunc.addItem(new Item(Utilizador.SEM_FUNCIONARIO, "(nenhum)"));
            for (Funcionario f : app.funcionarios.listar()) cbFunc.addItem(new Item(f.getId(), f.getId() + " - " + f.getNome()));
            if (sel != null) cbFunc.setSelectedItem(sel);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            modelo.setRowCount(0);
            for (Utilizador u : app.utilizadores.listar()) {
                String estado = !u.isAtivo() ? "Inactiva"
                        : u.getTentativasFalhadas() >= rh.servico.UtilizadorServico.MAX_TENTATIVAS ? "Bloqueada" : "Activa";
                modelo.addRow(new Object[]{u.getId(), u.getNomeUtilizador(), u.getPerfil(),
                        u.getFuncionarioId() == 0 ? "-" : app.funcionarios.nome(u.getFuncionarioId()), estado,
                        u.getUltimoAcesso() == null ? "nunca" : fmt.format(u.getUltimoAcesso())});
            }
            if (editandoId >= 0) Ui.seleccionar(tabela, editandoId);
        } finally {
            aCarregar = false;
        }
    }
}
