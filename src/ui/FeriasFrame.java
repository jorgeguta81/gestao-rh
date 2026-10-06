package rh.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import rh.modelo.*;
import rh.servico.FeriasServico;

/** TELA 3 - Férias. ACÇÃO 2: Solicitar férias (valida o limite de dias úteis). */
public class FeriasFrame extends JFrame {
    private final JComboBox<Funcionario> cbFunc = new JComboBox<>();
    private final JTextField tfInicio = new JTextField(LocalDate.now().toString(), 10);
    private final JTextField tfFim = new JTextField(LocalDate.now().plusDays(7).toString(), 10);
    private final DefaultTableModel modelo = Ui.modeloTabela(
            "ID", "Funcionário", "Início", "Fim", "Dias úteis", "Estado");

    public FeriasFrame() {
        super("Férias");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        cbFunc.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof Funcionario fu) setText(fu.getId() + " - " + fu.getNome());
                return this;
            }
        });

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Solicitar férias"));
        Ui.linha(form, 0, new JLabel("Funcionário:"), cbFunc);
        Ui.linha(form, 1, new JLabel("Início (AAAA-MM-DD):"), tfInicio);
        Ui.linha(form, 2, new JLabel("Fim (AAAA-MM-DD):"), tfFim);
        Ui.linha(form, 3, new JLabel("Limite anual:"),
                new JLabel(FeriasServico.LIMITE_DIAS_UTEIS_ANO + " dias úteis"));

        JButton bSolicitar = new JButton("Solicitar");
        bSolicitar.addActionListener(e -> solicitar());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(bSolicitar);

        JPanel topo = new JPanel(new BorderLayout());
        topo.add(form, BorderLayout.CENTER);
        topo.add(botoes, BorderLayout.SOUTH);

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(modelo)), BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override public void windowActivated(WindowEvent e) { carregar(); }
        });

        carregar();
        setSize(620, 450);
        setLocationRelativeTo(null);
    }

    private void solicitar() {
        try {
            Funcionario f = (Funcionario) cbFunc.getSelectedItem();
            if (f == null) throw new IllegalArgumentException("Escolha um funcionário.");
            LocalDate inicio = LocalDate.parse(tfInicio.getText().trim());
            LocalDate fim = LocalDate.parse(tfFim.getText().trim());

            Ferias pedido = Contexto.ferias.solicitar(f.getId(), inicio, fim);
            carregar();
            Ui.info(this, "Pedido #" + pedido.getId() + " registado (" + pedido.diasUteis()
                    + " dias úteis). Estado: " + pedido.getEstado() + ".");
        } catch (DateTimeParseException ex) {
            Ui.erro(this, "Data inválida. Use o formato AAAA-MM-DD, por exemplo 2026-12-15.");
        } catch (IllegalArgumentException ex) {
            Ui.erro(this, ex.getMessage());
        }
    }

    private void carregar() {
        Object seleccionado = cbFunc.getSelectedItem();
        cbFunc.removeAllItems();
        Contexto.funcionarios.listar().stream().filter(Funcionario::isAtivo).forEach(cbFunc::addItem);
        if (seleccionado != null) cbFunc.setSelectedItem(seleccionado);

        modelo.setRowCount(0);
        for (Ferias fe : Contexto.ferias.listar()) {
            modelo.addRow(new Object[]{fe.getId(), Contexto.nomeFuncionario(fe.getFuncionarioId()),
                    fe.getInicio(), fe.getFim(), fe.diasUteis(), fe.getEstado()});
        }
    }
}
