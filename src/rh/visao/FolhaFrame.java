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

        add(new JScrollPane(new JTable(modelo)), BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);


        atualizar();
        dimensionar(720, 380);
    }

    @Override
    protected void atualizar() {
        modelo.setRowCount(0);
        double totalBruto = 0, totalLiquido = 0, totalCusto = 0;
        for (LinhaFolha l : app.folha.linhas()) {
            modelo.addRow(new Object[]{l.id(), l.nome(), l.tipo(),
                    Ui.dinheiro(l.bruto()), Ui.dinheiro(l.inss()), Ui.dinheiro(l.irps()),
                    Ui.dinheiro(l.liquido()), Ui.dinheiro(l.custoEmpresa())});
            totalBruto += l.bruto();
            totalLiquido += l.liquido();
            totalCusto += l.custoEmpresa();
        }
        total.setText("TOTAL: Bruto " + Ui.dinheiro(totalBruto) + " MT | Líquido " + Ui.dinheiro(totalLiquido)
                + " MT | Custo para a empresa " + Ui.dinheiro(totalCusto) + " MT");
    }
}
