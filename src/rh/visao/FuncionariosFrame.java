package rh.visao;

import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import rh.modelo.*;
import rh.servico.DadosFuncionario;

/** TELA - Funcionários: listar, admitir, editar, demitir/reactivar e remover. */
public class FuncionariosFrame extends TelaBase {
    private final JComboBox<TipoFuncionario> cbTipo = new JComboBox<>(TipoFuncionario.values());
    private final JTextField tfNome = new JTextField(18), tfBi = new JTextField(18),
            tfEmail = new JTextField(18), tfTel = new JTextField(18), tfCargo = new JTextField(18),
            tfAdmissao = new JTextField(18),
            tfV1 = new JTextField(10), tfV2 = new JTextField(10), tfV3 = new JTextField(10);
    private final JLabel lbV1 = new JLabel(), lbV2 = new JLabel(), lbV3 = new JLabel();
    private final JComboBox<Departamento> cbDep = new JComboBox<>();
    private final DefaultTableModel modelo = Ui.modeloTabela(
            "ID", "Tipo", "Nome", "BI", "Cargo", "Departamento", "Estado", "Salário bruto");
    private final JTable tabela = new JTable(modelo);
    private final JButton bGravar = new JButton("Admitir");
    private final JButton bEstado = new JButton("Demitir");
    private final JButton bRemover = new JButton("Remover");
    private final JButton bNovo = new JButton("Novo");

    private int editandoId = -1;      // -1 = a admitir um novo funcionário
    private boolean aCarregar = false; // evita reagir à selecção durante o recarregamento

    public FuncionariosFrame(Navegador nav) {
        super("Funcionários", nav);

        cbDep.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof Departamento d) setText(d.getNome());
                return this;
            }
        });

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Dados do funcionário"));
        Ui.linha(form, 0, new JLabel("Tipo:"), cbTipo);
        Ui.linha(form, 1, new JLabel("Nome:"), tfNome);
        Ui.linha(form, 2, new JLabel("BI:"), tfBi);
        Ui.linha(form, 3, new JLabel("Email:"), tfEmail);
        Ui.linha(form, 4, new JLabel("Telefone:"), tfTel);
        Ui.linha(form, 5, new JLabel("Cargo:"), tfCargo);
        Ui.linha(form, 6, new JLabel("Departamento:"), cbDep);
        Ui.linha(form, 7, new JLabel("Admissão (AAAA-MM-DD):"), tfAdmissao);
        Ui.linha(form, 8, lbV1, tfV1);
        Ui.linha(form, 9, lbV2, tfV2);
        Ui.linha(form, 10, lbV3, tfV3);

        bNovo.addActionListener(e -> novo());
        bGravar.addActionListener(e -> gravar());
        bEstado.addActionListener(e -> alterarEstado());
        bRemover.addActionListener(e -> remover());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(bNovo);
        botoes.add(bGravar);
        botoes.add(bEstado);
        botoes.add(bRemover);

        JPanel esquerda = new JPanel(new BorderLayout());
        esquerda.add(form, BorderLayout.NORTH);
        esquerda.add(botoes, BorderLayout.CENTER);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Ui.larguras(tabela, 40, 80, 150, 120, 120, 120, 70, 100);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !aCarregar) aoSeleccionar();
        });

        add(esquerda, BorderLayout.WEST);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(rodapeVoltar(), BorderLayout.SOUTH);

        cbTipo.addActionListener(e -> ajustarCampos());
        ajustarCampos();
        atualizar();
        novo();
        dimensionar(1080, 560);
    }

    /** Mostra só os campos de valores que fazem sentido para o tipo escolhido. */
    private void ajustarCampos() {
        TipoFuncionario t = (TipoFuncionario) cbTipo.getSelectedItem();
        JLabel[] labels = {lbV1, lbV2, lbV3};
        JTextField[] campos = {tfV1, tfV2, tfV3};
        for (int i = 0; i < 3; i++) {
            String r = t.rotuloValor(i);
            labels[i].setText(r == null ? "" : r + ":");
            labels[i].setVisible(r != null);
            campos[i].setVisible(r != null);
        }
    }

    private DadosFuncionario lerFormulario() {
        TipoFuncionario t = (TipoFuncionario) cbTipo.getSelectedItem();
        Departamento dep = (Departamento) cbDep.getSelectedItem();
        if (dep == null) throw new IllegalArgumentException("Escolha um departamento.");
        double v1 = 0, v2 = 0, v3 = 0;
        if (t.rotuloValor(0) != null) v1 = Ui.numero(tfV1, t.rotuloValor(0));
        if (t.rotuloValor(1) != null) v2 = t == TipoFuncionario.CONTRATADO ? Ui.inteiro(tfV2, t.rotuloValor(1)) : Ui.numero(tfV2, t.rotuloValor(1));
        if (t.rotuloValor(2) != null) v3 = Ui.numero(tfV3, t.rotuloValor(2));
        return new DadosFuncionario(t, tfNome.getText().trim(), tfBi.getText().trim(), tfEmail.getText().trim(),
                tfTel.getText().trim(), tfCargo.getText().trim(), dep.getId(), Ui.data(tfAdmissao, "Admissão"), v1, v2, v3);
    }

    private void gravar() {
        try {
            DadosFuncionario d = lerFormulario();
            if (editandoId < 0) {
                Funcionario f = app.funcionarios.admitir(d);
                Ui.info(this, "Funcionário admitido com o ID " + f.getId() + ".");
            } else {
                app.funcionarios.atualizar(editandoId, d);
                Ui.info(this, "Funcionário actualizado.");
            }
            novo();
            atualizar();
        } catch (IllegalArgumentException ex) {
            Ui.erro(this, ex.getMessage());
        }
    }

    private void alterarEstado() {
        try {
            Funcionario f = app.funcionarios.buscar(editandoId).orElseThrow();
            if (f.isAtivo()) {
                if (!Ui.confirmar(this, "Demitir " + f.getNome() + "? Fica inactivo mas o histórico mantém-se.")) return;
                app.funcionarios.demitir(editandoId);
            } else {
                app.funcionarios.reativar(editandoId);
            }
            atualizar();
            aoSeleccionar();
        } catch (IllegalArgumentException ex) {
            Ui.erro(this, ex.getMessage());
        }
    }

    private void remover() {
        try {
            if (!Ui.confirmar(this, "Remover definitivamente este funcionário? Esta acção não pode ser desfeita.")) return;
            app.funcionarios.remover(editandoId);
            novo();
            atualizar();
        } catch (IllegalArgumentException ex) {
            Ui.erro(this, ex.getMessage());
        }
    }

    /** Limpa o formulário e volta ao modo "admitir". */
    private void novo() {
        editandoId = -1;
        tabela.clearSelection();
        for (JTextField t : new JTextField[]{tfNome, tfBi, tfEmail, tfTel, tfCargo, tfV1, tfV2, tfV3}) t.setText("");
        tfAdmissao.setText(LocalDate.now().toString());
        cbTipo.setEnabled(true);
        cbTipo.setSelectedIndex(0);
        modoBotoes();
    }

    private void aoSeleccionar() {
        int id = Ui.idSeleccionado(tabela);
        if (id < 0) { editandoId = -1; modoBotoes(); return; }
        Funcionario f = app.funcionarios.buscar(id).orElse(null);
        if (f == null) return;
        editandoId = id;
        DadosFuncionario d = DadosFuncionario.de(f);
        cbTipo.setSelectedItem(d.tipo());
        cbTipo.setEnabled(false); // o tipo não se altera depois de admitido
        tfNome.setText(d.nome()); tfBi.setText(d.bi()); tfEmail.setText(d.email());
        tfTel.setText(d.telefone()); tfCargo.setText(d.cargo());
        tfAdmissao.setText(String.valueOf(d.dataAdmissao()));
        for (int i = 0; i < cbDep.getItemCount(); i++)
            if (cbDep.getItemAt(i).getId() == d.departamentoId()) cbDep.setSelectedIndex(i);
        tfV1.setText(texto(d.valor1()));
        tfV2.setText(d.tipo() == TipoFuncionario.CONTRATADO ? String.valueOf((int) d.valor2()) : texto(d.valor2()));
        tfV3.setText(texto(d.valor3()));
        bEstado.setText(f.isAtivo() ? "Demitir" : "Reactivar");
        modoBotoes();
    }

    private static String texto(double v) { return String.valueOf(v); }

    private void modoBotoes() {
        boolean edicao = editandoId >= 0;
        bGravar.setText(edicao ? "Guardar" : "Admitir");
        bEstado.setEnabled(edicao);
        bRemover.setEnabled(edicao);
        if (!edicao) bEstado.setText("Demitir");
    }

    @Override
    protected void atualizar() {
        aCarregar = true;
        try {
            Object dep = cbDep.getSelectedItem();
            cbDep.removeAllItems();
            app.departamentos.listar().forEach(cbDep::addItem);
            if (dep != null) cbDep.setSelectedItem(dep);

            modelo.setRowCount(0);
            for (Funcionario f : app.funcionarios.listar()) {
                modelo.addRow(new Object[]{f.getId(), f.getTipo(), f.getNome(), f.getBi(), f.getCargo(),
                        app.departamentos.nome(f.getDepartamentoId()),
                        f.isAtivo() ? "Activo" : "Inactivo", Ui.dinheiro(f.calcularSalarioBruto())});
            }
            if (editandoId >= 0) Ui.seleccionar(tabela, editandoId);
        } finally {
            aCarregar = false;
        }
    }
}
