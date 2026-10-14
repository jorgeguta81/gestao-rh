package rh.visao;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import rh.servico.FolhaSalarioServico.LinhaFolha;

/** TELA 4 - Folha de salários (só consulta; usa o polimorfismo de calcularSalarioBruto/Inss). */
public class FolhaFrame extends TelaBase {
    private final DefaultTableModel modelo = Ui.modeloTabela(
            "ID", "Nome", "Tipo", "Bruto (MT)", "INSS 3% (MT)", "IRPS (MT)", "Líquido (MT)", "Custo empresa (MT)");
    private final JTable tabela;
    private final JLabel total = new JLabel(" ");
    private final JTextField tfBusca = new JTextField(14);
    private final JComboBox<Object> cbTipo = new JComboBox<>();

    public FolhaFrame(Navegador nav) {
        super(nav.app().sessao.tem(rh.modelo.Permissao.FOLHA_VER_TODOS) ? "Folha de salários" : "O meu recibo", nav);

        JButton bRecibo = new JButton("Ver recibo");
        bRecibo.addActionListener(e -> verRecibo());
        JButton bActualizar = new JButton("Actualizar");
        bActualizar.addActionListener(e -> atualizar());

        total.setFont(total.getFont().deriveFont(Font.BOLD));
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        rodape.add(total, BorderLayout.CENTER);
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acoes.add(bRecibo);
        acoes.add(bActualizar);
        acoes.add(botaoVoltar());
        rodape.add(acoes, BorderLayout.EAST);

        tabela = Ui.configurar(new JTable(modelo));
        Ui.larguras(tabela, 40, 170, 90, 100, 100, 100, 110, 130);
        cbTipo.addItem("Todos os tipos");
        for (rh.modelo.TipoFuncionario t : rh.modelo.TipoFuncionario.values()) cbTipo.addItem(t);
        Ui.aoEscrever(tfBusca, this::atualizar);
        cbTipo.addActionListener(e -> atualizar());
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        filtros.setBorder(BorderFactory.createTitledBorder("Pesquisar e filtrar (clique nos cabeçalhos para ordenar)"));
        filtros.add(new JLabel("Nome:")); filtros.add(tfBusca); filtros.add(cbTipo);
        filtros.setVisible(app.sessao.tem(rh.modelo.Permissao.FOLHA_VER_TODOS));
        add(filtros, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);


        atualizar();
        dimensionar(1000, 400);
    }

    /** Mostra o recibo de vencimento do mês actual para a linha seleccionada (ou para o próprio, se só houver uma). */
    private void verRecibo() {
        Ui.tentar(this, () -> {
            int id = Ui.idSeleccionado(tabela);
            if (id < 0 && tabela.getRowCount() == 1) id = (Integer) tabela.getValueAt(0, 0);
            if (id < 0) throw new rh.excecao.ValidacaoException("Seleccione um funcionário na tabela.");
            String texto = app.folha.recibo(id, java.time.YearMonth.now());

            JTextArea area = new JTextArea(texto);
            area.setEditable(false);
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            JScrollPane sp = new JScrollPane(area);
            sp.setPreferredSize(new Dimension(560, 420));
            Object[] opcoes = {"Guardar como .txt", "Fechar"};
            int r = JOptionPane.showOptionDialog(this, sp, "Recibo de vencimento", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, opcoes, opcoes[1]);
            if (r == 0) {
                JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(new java.io.File("recibo-" + id + "-" + java.time.YearMonth.now() + ".txt"));
                if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        java.nio.file.Files.writeString(fc.getSelectedFile().toPath(), texto, java.nio.charset.StandardCharsets.UTF_8);
                    } catch (java.io.IOException ex) {
                        throw new rh.excecao.PersistenciaException("Não foi possível gravar o recibo.", ex);
                    }
                }
            }
        });
    }

    @Override
    protected void atualizar() {
        modelo.setRowCount(0);
        double totalBruto = 0, totalLiquido = 0, totalCusto = 0;
        Object t = cbTipo.getSelectedItem();
        for (LinhaFolha l : app.folha.linhas(tfBusca.getText(), t instanceof rh.modelo.TipoFuncionario tf ? tf : null)) {
            modelo.addRow(new Object[]{l.id(), l.nome(), l.tipo(),
                    l.bruto(), l.inss(), l.irps(), l.liquido(), l.custoEmpresa()});
            totalBruto += l.bruto();
            totalLiquido += l.liquido();
            totalCusto += l.custoEmpresa();
        }
        total.setText("Totais (MT): bruto " + Ui.dinheiro(totalBruto) + " | líquido " + Ui.dinheiro(totalLiquido)
                + " | custo empresa " + Ui.dinheiro(totalCusto));
    }
}
