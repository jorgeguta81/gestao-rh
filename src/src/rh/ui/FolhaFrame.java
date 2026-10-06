package rh.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import rh.modelo.Funcionario;

/** TELA 4 - Folha de salários (só consulta; usa o polimorfismo de calcularSalarioBruto/Inss). */
public class FolhaFrame extends JFrame {
    private final DefaultTableModel modelo = Ui.modeloTabela(
            "ID", "Nome", "Tipo", "Bruto (MT)", "INSS (MT)", "Líquido (MT)");
    private final JLabel total = new JLabel(" ");

    public FolhaFrame() {
        super("Folha de salários");
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
        for (Funcionario f : Contexto.funcionarios.listar()) {
            if (!f.isAtivo()) continue;
            modelo.addRow(new Object[]{f.getId(), f.getNome(), f.getTipo(),
                    Ui.dinheiro(f.calcularSalarioBruto()), Ui.dinheiro(f.calcularInss()),
                    Ui.dinheiro(f.calcularSalarioLiquido())});
            totalBruto += f.calcularSalarioBruto();
            totalLiquido += f.calcularSalarioLiquido();
        }
        total.setText("TOTAL: Bruto " + Ui.dinheiro(totalBruto) + " MT | Líquido " + Ui.dinheiro(totalLiquido) + " MT");
    }
}
