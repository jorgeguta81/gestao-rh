package rh.visao;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import rh.modelo.Departamento;
import rh.modelo.Permissao;

/** TELA - Departamentos: criar, editar e remover. */
public class DepartamentosFrame extends TelaBase {
    private final DefaultTableModel modelo = Ui.modeloTabela("ID", "Nome", "Descrição");
    private final JTable tabela = Ui.configurar(new JTable(modelo));
    private final JTextField tfBusca = new JTextField(18);
    private final JTextField tfNome = new JTextField(20), tfDescricao = new JTextField(20);
    private final JButton bGravar = new JButton("Criar"), bRemover = new JButton("Remover"), bNovo = new JButton("Novo");
    private int editandoId = -1;
    private boolean aCarregar = false;

    public DepartamentosFrame(Navegador nav) {
        super("Departamentos", nav);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Departamento"));
        Ui.linha(form, 0, new JLabel("Nome:"), tfNome);
        Ui.linha(form, 1, new JLabel("Descrição:"), tfDescricao);
        bNovo.addActionListener(e -> novo());
        bGravar.addActionListener(e -> gravar());
        bRemover.addActionListener(e -> remover());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(bNovo); botoes.add(bGravar); botoes.add(bRemover);
        JPanel topo = new JPanel(new BorderLayout());
        topo.add(form, BorderLayout.CENTER);
        topo.add(botoes, BorderLayout.SOUTH);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !aCarregar) aoSeleccionar();
        });

        Ui.aoEscrever(tfBusca, this::atualizar);
        JPanel busca = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        busca.setBorder(BorderFactory.createTitledBorder("Pesquisar"));
        busca.add(new JLabel("Nome ou descrição:")); busca.add(tfBusca);
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(busca, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(topo, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(rodapeVoltar(), BorderLayout.SOUTH);
        atualizar();
        novo();
        dimensionar(640, 520);
    }

    private void gravar() {
        Ui.tentar(this, () -> {
            if (editandoId < 0) app.departamentos.criar(tfNome.getText(), tfDescricao.getText());
            else app.departamentos.atualizar(editandoId, tfNome.getText(), tfDescricao.getText());
            novo();
            atualizar();
        });
    }

    private void remover() {
        Ui.tentar(this, () -> {
            if (!Ui.confirmar(this, "Remover este departamento?")) return;
            app.departamentos.remover(editandoId);
            novo();
            atualizar();
        });
    }

    private void novo() {
        editandoId = -1;
        tabela.clearSelection();
        tfNome.setText(""); tfDescricao.setText("");
        modoBotoes();
    }

    private void aoSeleccionar() {
        int id = Ui.idSeleccionado(tabela);
        if (id < 0) { editandoId = -1; modoBotoes(); return; }
        editandoId = id;
        int r = tabela.getSelectedRow();
        tfNome.setText(String.valueOf(tabela.getValueAt(r, 1)));
        tfDescricao.setText(String.valueOf(tabela.getValueAt(r, 2)));
        modoBotoes();
    }

    private void modoBotoes() {
        boolean edita = app.sessao.tem(Permissao.DEPARTAMENTOS_EDITAR);
        bGravar.setText(editandoId >= 0 ? "Guardar" : "Criar");
        bGravar.setEnabled(edita);
        bRemover.setEnabled(editandoId >= 0 && edita);
    }

    @Override
    protected void atualizar() {
        aCarregar = true;
        try {
            modelo.setRowCount(0);
            for (Departamento d : app.departamentos.pesquisar(tfBusca.getText()))
                modelo.addRow(new Object[]{d.getId(), d.getNome(), d.getDescricao()});
            if (editandoId >= 0) Ui.seleccionar(tabela, editandoId);
        } finally {
            aCarregar = false;
        }
    }
}
