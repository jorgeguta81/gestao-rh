package rh.visao;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import rh.Aplicacao;
import rh.servico.FolhaSalarioServico.LinhaFolha;

/** TELA 4 - Folha de salários (só consulta; usa o polimorfismo de calcularSalarioBruto/Inss). */
public class FolhaFrame extends JFrame {
    private final DefaultTableModel modelo = Ui.modeloTabela(
            "ID", "Nome", "Tipo", "Bruto (MT)", "INSS (MT)", "Líquido (MT)");
    private final JLabel total = new JLabel(" ");

    private final Aplicacao app;

    public FolhaFrame(Aplicacao app) {
        super("Folha de salários");
        this.app = app;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JButton bActualizar = new JButton("Actualizar");
        bActualizar.addActionListener(e -> carregar());

        total.setFont(total.getFont().deriveFont(Font.BOLD));
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        rodape.add(total, BorderLayout.CENTER);
        rodape.add(bActualizar, BorderLayout.EAST);

        add(new JScrollPane(new JTable(modelo)), BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override public void windowActivated(WindowEvent e) { carregar(); }
        });

        carregar();
        setSize(720, 380);
        setLocationRelativeTo(null);
    }

    private void carregar() {
        modelo.setRowCount(0);
        double totalBruto = 0, totalLiquido = 0;
        for (LinhaFolha l : app.folha.linhas()) {
            modelo.addRow(new Object[]{l.id(), l.nome(), l.tipo(),
                    Ui.dinheiro(l.bruto()), Ui.dinheiro(l.inss()), Ui.dinheiro(l.liquido())});
            totalBruto += l.bruto();
            totalLiquido += l.liquido();
        }
        total.setText("TOTAL: Bruto " + Ui.dinheiro(totalBruto) + " MT | Líquido " + Ui.dinheiro(totalLiquido) + " MT");
    }
}
