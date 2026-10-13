package rh.visao;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import rh.modelo.*;
import rh.servico.FeriasServico;

/** TELA 3 - Férias. ACÇÃO 2: Solicitar férias (valida o limite de dias úteis). */
public class FeriasFrame extends TelaBase {
    private final JComboBox<Funcionario> cbFunc = new JComboBox<>();
    private final JTextField tfInicio = new JTextField(LocalDate.now().toString(), 10);
    private final JTextField tfFim = new JTextField(LocalDate.now().plusDays(7).toString(), 10);
    private final DefaultTableModel modelo = Ui.modeloTabela(
            "ID", "Funcionário", "Início", "Fim", "Dias úteis", "Estado");

    public FeriasFrame(Navegador nav) {
        super("Férias", nav);

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
        add(rodapeVoltar(), BorderLayout.SOUTH);


        atualizar();
        dimensionar(620, 450);
    }

    private void solicitar() {
        try {
            Funcionario f = (Funcionario) cbFunc.getSelectedItem();
            if (f == null) throw new IllegalArgumentException("Escolha um funcionário.");
            LocalDate inicio = LocalDate.parse(tfInicio.getText().trim());
            LocalDate fim = LocalDate.parse(tfFim.getText().trim());

            Ferias pedido = app.ferias.solicitar(f.getId(), inicio, fim);
            atualizar();
            Ui.info(this, "Pedido #" + pedido.getId() + " registado (" + pedido.diasUteis()
                    + " dias úteis). Estado: " + pedido.getEstado() + ".");
        } catch (DateTimeParseException ex) {
            Ui.erro(this, "Data inválida. Use o formato AAAA-MM-DD, por exemplo 2026-12-15.");
        } catch (IllegalArgumentException ex) {
            Ui.erro(this, ex.getMessage());
        }
    }

    @Override
    protected void atualizar() {
        Object seleccionado = cbFunc.getSelectedItem();
        cbFunc.removeAllItems();
        app.funcionarios.listarActivos().forEach(cbFunc::addItem);
        if (seleccionado != null) cbFunc.setSelectedItem(seleccionado);

        modelo.setRowCount(0);
        for (Ferias fe : app.ferias.listar()) {
            modelo.addRow(new Object[]{fe.getId(), app.funcionarios.nome(fe.getFuncionarioId()),
                    fe.getInicio(), fe.getFim(), fe.diasUteis(), fe.getEstado()});
        }
    }
}
