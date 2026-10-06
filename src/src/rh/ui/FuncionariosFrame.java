package rh.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import rh.modelo.*;

/** TELA 2 - Funcionários. ACÇÃO 1: Admitir funcionário. */
public class FuncionariosFrame extends JFrame {
    private final JComboBox<String> cbTipo =
            new JComboBox<>(new String[]{"Efectivo", "Gestor", "Contratado", "Estagiário"});
    private final JTextField tfNome = new JTextField(18), tfBi = new JTextField(18),
            tfEmail = new JTextField(18), tfTel = new JTextField(18), tfCargo = new JTextField(18),
            tfV1 = new JTextField(10), tfV2 = new JTextField(10), tfV3 = new JTextField(10);
    private final JLabel lbV1 = new JLabel(), lbV2 = new JLabel(), lbV3 = new JLabel();
    private final JComboBox<Departamento> cbDep = new JComboBox<>();
    private final DefaultTableModel modelo = Ui.modeloTabela(
            "ID", "Tipo", "Nome", "BI", "Cargo", "Departamento", "Estado", "Salário bruto");

    public FuncionariosFrame() {
        super("Funcionários");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        cbDep.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof Departamento d) setText(d.getNome());
                return this;
            }
        });

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Admitir funcionário"));
        Ui.linha(form, 0, new JLabel("Tipo:"), cbTipo);
        Ui.linha(form, 1, new JLabel("Nome:"), tfNome);
        Ui.linha(form, 2, new JLabel("BI:"), tfBi);
        Ui.linha(form, 3, new JLabel("Email:"), tfEmail);
        Ui.linha(form, 4, new JLabel("Telefone:"), tfTel);
        Ui.linha(form, 5, new JLabel("Cargo:"), tfCargo);
        Ui.linha(form, 6, new JLabel("Departamento:"), cbDep);
        Ui.linha(form, 7, lbV1, tfV1);
        Ui.linha(form, 8, lbV2, tfV2);
        Ui.linha(form, 9, lbV3, tfV3);

        JButton bAdmitir = new JButton("Admitir");
        JButton bLimpar = new JButton("Limpar");
        bAdmitir.addActionListener(e -> admitir());
        bLimpar.addActionListener(e -> limpar());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(bLimpar);
        botoes.add(bAdmitir);

        JPanel esquerda = new JPanel(new BorderLayout());
        esquerda.add(form, BorderLayout.NORTH);
        esquerda.add(botoes, BorderLayout.CENTER);

        add(esquerda, BorderLayout.WEST);
        add(new JScrollPane(new JTable(modelo)), BorderLayout.CENTER);

        cbTipo.addActionListener(e -> ajustarCampos());
        addWindowListener(new WindowAdapter() {
            @Override public void windowActivated(WindowEvent e) { carregar(); }
        });

        ajustarCampos();
        carregar();
        setSize(960, 480);
        setLocationRelativeTo(null);
    }

    /** Mostra só os campos de valores que fazem sentido para o tipo escolhido. */
    private void ajustarCampos() {
        int t = cbTipo.getSelectedIndex();
        switch (t) {
            case 0 -> { lbV1.setText("Salário base:"); lbV2.setText("Subsídios:"); lbV3.setText(""); }
            case 1 -> { lbV1.setText("Salário base:"); lbV2.setText("Subsídios:"); lbV3.setText("Bónus:"); }
            case 2 -> { lbV1.setText("Valor/hora:"); lbV2.setText("Horas trabalhadas:"); lbV3.setText(""); }
            default -> { lbV1.setText("Bolsa:"); lbV2.setText(""); lbV3.setText(""); }
        }
        tfV2.setVisible(t != 3);
        lbV2.setVisible(t != 3);
        tfV3.setVisible(t == 1);
        lbV3.setVisible(t == 1);
    }

    private void admitir() {
        try {
            String nome = tfNome.getText().trim();
            String bi = tfBi.getText().trim();
            if (nome.isEmpty()) throw new IllegalArgumentException("O nome é obrigatório.");
            if (bi.isEmpty()) throw new IllegalArgumentException("O BI é obrigatório.");
            Departamento dep = (Departamento) cbDep.getSelectedItem();
            if (dep == null) throw new IllegalArgumentException("Escolha um departamento.");

            String email = tfEmail.getText().trim(), tel = tfTel.getText().trim(), cargo = tfCargo.getText().trim();
            LocalDate hoje = LocalDate.now();

            Funcionario f = switch (cbTipo.getSelectedIndex()) {
                case 0 -> new FuncionarioEfetivo(nome, bi, email, tel, cargo, dep.getId(), hoje,
                        Ui.numero(tfV1, "Salário base"), Ui.numero(tfV2, "Subsídios"));
                case 1 -> new Gestor(nome, bi, email, tel, cargo, dep.getId(), hoje,
                        Ui.numero(tfV1, "Salário base"), Ui.numero(tfV2, "Subsídios"), Ui.numero(tfV3, "Bónus"));
                case 2 -> new FuncionarioContratado(nome, bi, email, tel, cargo, dep.getId(), hoje,
                        Ui.numero(tfV1, "Valor/hora"), Ui.inteiro(tfV2, "Horas trabalhadas"));
                default -> new Estagiario(nome, bi, email, tel, cargo, dep.getId(), hoje,
                        Ui.numero(tfV1, "Bolsa"));
            };

            Funcionario criado = Contexto.funcionarios.admitir(f);
            carregar();
            limpar();
            Ui.info(this, "Funcionário admitido com o ID " + criado.getId() + ".");
        } catch (IllegalArgumentException ex) {
            Ui.erro(this, ex.getMessage());
        }
    }

    private void limpar() {
        for (JTextField t : new JTextField[]{tfNome, tfBi, tfEmail, tfTel, tfCargo, tfV1, tfV2, tfV3}) t.setText("");
        cbTipo.setSelectedIndex(0);
    }

    private void carregar() {
        Object seleccionado = cbDep.getSelectedItem();
        cbDep.removeAllItems();
        Contexto.departamentos.listarTodos().forEach(cbDep::addItem);
        if (seleccionado != null) cbDep.setSelectedItem(seleccionado);

        modelo.setRowCount(0);
        for (Funcionario f : Contexto.funcionarios.listar()) {
            modelo.addRow(new Object[]{f.getId(), f.getTipo(), f.getNome(), f.getBi(), f.getCargo(),
                    Contexto.nomeDepartamento(f.getDepartamentoId()),
                    f.isAtivo() ? "Activo" : "Inactivo", Ui.dinheiro(f.calcularSalarioBruto())});
        }
    }
}
