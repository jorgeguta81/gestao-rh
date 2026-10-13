package rh.visao;

import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import rh.modelo.Departamento;

/** TELA - Departamentos (por agora só listagem; o CRUD completo chega com a integração da interface). */
public class DepartamentosFrame extends TelaBase {
    private final DefaultTableModel modelo = Ui.modeloTabela("ID", "Nome", "Descrição");

    public DepartamentosFrame(Navegador nav) {
        super("Departamentos", nav);
        add(new JScrollPane(new JTable(modelo)), BorderLayout.CENTER);
        add(rodapeVoltar(), BorderLayout.SOUTH);
        atualizar();
        dimensionar(560, 360);
    }

    @Override
    protected void atualizar() {
        modelo.setRowCount(0);
        for (Departamento d : app.departamentos.listar()) {
            modelo.addRow(new Object[]{d.getId(), d.getNome(), d.getDescricao()});
        }
    }
}
