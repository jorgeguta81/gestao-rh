package rh.visao;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import rh.servico.FolhaSalarioServico.LinhaFolha;

/** TELA 4 - Folha de salários (só consulta; usa o polimorfismo de calcularSalarioBruto/Inss). */
public class FolhaFrame extends TelaBase {
    private final DefaultTableModel modelo = Ui.modeloTabela(
            "ID", "Nome", "Tipo", "Bruto (MT)", "INSS 3% (MT)", "IRPS (MT)", "Líquido (MT)", "Custo empresa (MT)");
    private final JLabel total = new JLabel(" ");
    private final JTextField tfBusca = new JTextField(14);
    private final JComboBox<Object> cbTipo = new JComboBox<>();

    public FolhaFrame(Navegador nav) {
        super(nav.app().sessao.tem(rh.modelo.Permissao.FOLHA_VER_TODOS) ? "Folha de salários" : "O meu recibo", nav);

        JButton bActualizar = new JButton("Actualizar");
        bActualizar.addActionListener(e -> atualizar());

        total.setFont(total.getFont().deriveFont(Font.BOLD));
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        rodape.add(total, BorderLayout.CENTER);
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acoes.add(bActualizar);
        acoes.add(botaoVoltar());
        rodape.add(acoes, BorderLayout.EAST);

        JTable tabela = Ui.configurar(new JTable(modelo));
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
