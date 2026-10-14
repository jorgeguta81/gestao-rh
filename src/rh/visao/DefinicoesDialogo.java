package rh.visao;

import java.awt.Component;
import java.awt.GridBagLayout;
import javax.swing.*;
import rh.Aplicacao;
import rh.config.SectorActividade;

/** Definições da empresa: nome e sector de actividade (define o salário mínimo aplicado nas validações). */
final class DefinicoesDialogo {
    private DefinicoesDialogo() {}

    static void mostrar(Component pai, Aplicacao app) {
        JTextField nome = new JTextField(app.definicoes.nomeEmpresa(), 24);
        JComboBox<SectorActividade> sector = new JComboBox<>(SectorActividade.values());
        sector.setSelectedItem(app.definicoes.sector());
        JLabel minimo = new JLabel();
        Runnable actualiza = () -> minimo.setText(String.format("%,.2f MT/mês",
                ((SectorActividade) sector.getSelectedItem()).salarioMinimo()));
        sector.addActionListener(e -> actualiza.run());
        actualiza.run();

        JPanel p = new JPanel(new GridBagLayout());
        Ui.linha(p, 0, new JLabel("Empresa:"), nome);
        Ui.linha(p, 1, new JLabel("Sector de actividade:"), sector);
        Ui.linha(p, 2, new JLabel("Salário mínimo do sector:"), minimo);

        if (JOptionPane.showConfirmDialog(pai, p, "Definições da empresa", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        Ui.tentar(pai, () -> {
            app.definicoes.guardar(nome.getText(), (SectorActividade) sector.getSelectedItem());
            Ui.info(pai, "Definições guardadas.");
        });
    }
}
