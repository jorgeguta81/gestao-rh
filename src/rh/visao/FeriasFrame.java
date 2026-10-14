package rh.visao;

import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import rh.excecao.RhException;
import rh.servico.CriterioFerias;
import rh.excecao.ValidacaoException;
import rh.modelo.*;

/** TELA 3 - Férias. ACÇÃO 2: Solicitar férias (valida o limite de dias úteis). */
public class FeriasFrame extends TelaBase {
    private final JComboBox<Funcionario> cbFunc = new JComboBox<>();
    private final JSpinner spAno = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1));
    private final JLabel lbSaldo = new JLabel(" ");
    private final JTextField tfInicio = new JTextField(LocalDate.now().toString(), 10);
    private final JTextField tfFim = new JTextField(LocalDate.now().plusDays(7).toString(), 10);
    private final DefaultTableModel modelo = Ui.modeloTabela(
            "ID", "Funcionário", "Início", "Fim", "Dias de gozo", "Ano ref.", "Estado");
    private final JTable tabela = Ui.configurar(new JTable(modelo));
    // pesquisa, filtros e ordenação
    private final JTextField tfBusca = new JTextField(14);
    private final JComboBox<Object> cbFiltroEstado = new JComboBox<>();
    private final JComboBox<Object> cbFiltroAno = new JComboBox<>();
    private final JComboBox<CriterioFerias.Campo> cbOrdem = new JComboBox<>(CriterioFerias.Campo.values());
    private final JCheckBox ckDesc = new JCheckBox("Decrescente", true);
    private final JLabel lbContagem = new JLabel(" ");

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
        spAno.setEditor(new JSpinner.NumberEditor(spAno, "#")); // sem separador de milhares
        Ui.linha(form, 3, new JLabel("Ano de referência:"), spAno);
        Ui.linha(form, 4, new JLabel("Direito e saldo:"), lbSaldo);

        JButton bSolicitar = new JButton("Solicitar");
        bSolicitar.addActionListener(e -> solicitar());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(bSolicitar);

        JButton bAprovar = new JButton("Aprovar");
        JButton bRejeitar = new JButton("Rejeitar");
        JButton bCancelar = new JButton("Cancelar pedido");
        bAprovar.addActionListener(e -> accao(id -> app.ferias.decidir(id, true)));
        bRejeitar.addActionListener(e -> accao(id -> app.ferias.decidir(id, false)));
        bCancelar.addActionListener(e -> accao(id -> {
            if (Ui.confirmar(this, "Cancelar o pedido #" + id + "?")) app.ferias.cancelar(id);
        }));
        bAprovar.setVisible(app.sessao.tem(Permissao.FERIAS_DECIDIR));
        bRejeitar.setVisible(app.sessao.tem(Permissao.FERIAS_DECIDIR));
        JPanel decisao = new JPanel(new FlowLayout(FlowLayout.LEFT));
        decisao.add(new JLabel("Pedido seleccionado:"));
        decisao.add(bAprovar); decisao.add(bRejeitar); decisao.add(bCancelar);
        JPanel sul = new JPanel(new BorderLayout());
        sul.add(decisao, BorderLayout.WEST);
        sul.add(rodapeVoltar(), BorderLayout.EAST);

        JPanel topo = new JPanel(new BorderLayout());
        topo.add(form, BorderLayout.CENTER);
        topo.add(botoes, BorderLayout.SOUTH);

        add(topo, BorderLayout.NORTH);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cbFiltroEstado.addItem("Todos os estados");
        for (EstadoFerias e : EstadoFerias.values()) cbFiltroEstado.addItem(e);
        cbFiltroAno.addItem("Todos os anos");
        for (int a = LocalDate.now().getYear() + 1; a >= LocalDate.now().getYear() - 3; a--) cbFiltroAno.addItem(a);
        cbOrdem.setPrototypeDisplayValue(CriterioFerias.Campo.FUNCIONARIO);
        Runnable refiltrar = this::atualizar;
        Ui.aoEscrever(tfBusca, refiltrar);
        cbFiltroEstado.addActionListener(e -> refiltrar.run());
        cbFiltroAno.addActionListener(e -> refiltrar.run());
        cbOrdem.addActionListener(e -> { tabela.getRowSorter().setSortKeys(null); refiltrar.run(); });
        ckDesc.addActionListener(e -> { tabela.getRowSorter().setSortKeys(null); refiltrar.run(); });
        JButton bLimpar = new JButton("Limpar filtros");
        bLimpar.addActionListener(e -> {
            tfBusca.setText(""); cbFiltroEstado.setSelectedIndex(0); cbFiltroAno.setSelectedIndex(0);
            cbOrdem.setSelectedItem(CriterioFerias.Campo.INICIO); ckDesc.setSelected(true);
            tabela.getRowSorter().setSortKeys(null);
            atualizar();
        });
        boolean vePorNome = app.sessao.tem(Permissao.FERIAS_VER_TODAS);
        JPanel filtros = Ui.painelFiltros("Pesquisar, filtrar e ordenar",
                vePorNome ? new JComponent[]{new JLabel("Funcionário:"), tfBusca, cbFiltroEstado, cbFiltroAno}
                          : new JComponent[]{cbFiltroEstado, cbFiltroAno},
                new JComponent[]{new JLabel("Ordenar por:"), cbOrdem, ckDesc, bLimpar, lbContagem});

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(filtros, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
        add(sul, BorderLayout.SOUTH);


        cbFunc.addActionListener(e -> mostrarSaldo());
        spAno.addChangeListener(e -> mostrarSaldo());
        atualizar();
        dimensionar(900, 560);
    }

    /** Executa uma acção sobre o pedido seleccionado e recarrega a tabela. */
    private void accao(java.util.function.IntConsumer acao) {
        Ui.tentar(this, () -> {
            int id = Ui.idSeleccionado(tabela);
            if (id < 0) throw new ValidacaoException("Seleccione um pedido na tabela.");
            acao.accept(id);
            atualizar();
        });
    }

    /** Mostra o direito a férias e o saldo do funcionário escolhido no ano de referência. */
    private void mostrarSaldo() {
        Funcionario f = (Funcionario) cbFunc.getSelectedItem();
        if (f == null) { lbSaldo.setText(" "); return; }
        try {
            var s = app.ferias.saldo(f.getId(), (Integer) spAno.getValue());
            lbSaldo.setText(s.direito() + " dias - " + s.usados() + " usados/pedidos = " + s.disponivel() + " disponíveis");
        } catch (RhException ex) {
            lbSaldo.setText(ex.getMessage()); // texto informativo, não um diálogo
        }
    }

    private void solicitar() {
        Ui.tentar(this, () -> {
            Funcionario f = (Funcionario) cbFunc.getSelectedItem();
            if (f == null) throw new ValidacaoException("Escolha um funcionário.");
            LocalDate inicio = Ui.data(tfInicio, "Início");
            LocalDate fim = Ui.data(tfFim, "Fim");

            Ferias pedido = app.ferias.solicitar(f.getId(), inicio, fim, (Integer) spAno.getValue());
            atualizar();
            Ui.info(this, "Pedido #" + pedido.getId() + " registado (" + pedido.getDiasGozo()
                    + " dias de gozo, feriados excluídos). Estado: " + pedido.getEstado() + ".");
        });
    }

    @Override
    protected void atualizar() {
        Object seleccionado = cbFunc.getSelectedItem();
        cbFunc.removeAllItems();
        app.funcionarios.listarActivos().forEach(cbFunc::addItem);
        if (seleccionado != null) cbFunc.setSelectedItem(seleccionado);
        mostrarSaldo();

        modelo.setRowCount(0);
        Object estado = cbFiltroEstado.getSelectedItem(), ano = cbFiltroAno.getSelectedItem();
        var lista = app.ferias.pesquisar(new CriterioFerias(tfBusca.getText(),
                estado instanceof EstadoFerias e ? e : null, ano instanceof Integer a ? a : null,
                (CriterioFerias.Campo) cbOrdem.getSelectedItem(), !ckDesc.isSelected()));
        lbContagem.setText(lista.size() + " pedido(s)");
        for (Ferias fe : lista) {
            modelo.addRow(new Object[]{fe.getId(), app.funcionarios.nome(fe.getFuncionarioId()),
                    fe.getInicio(), fe.getFim(), fe.getDiasGozo(), fe.getAnoReferencia(), fe.getEstado()});
        }
    }
}
