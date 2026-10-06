package rh.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Pequenas ajudas para não repetir código nas telas. */
final class Ui {
    private Ui() {}

    static void linha(JPanel painel, int linha, JComponent rotulo, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = linha;
        c.insets = new Insets(3, 6, 3, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        painel.add(rotulo, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        painel.add(campo, c);
    }

    static DefaultTableModel modeloTabela(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    static double numero(JTextField campo, String nome) {
        String t = campo.getText().trim().replace(',', '.');
        if (t.isEmpty()) throw new IllegalArgumentException("Preencha o campo \"" + nome + "\".");
        try {
            double v = Double.parseDouble(t);
            if (v < 0) throw new IllegalArgumentException("\"" + nome + "\" não pode ser negativo.");
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + nome + "\" deve ser um número.");
        }
    }

    static int inteiro(JTextField campo, String nome) {
        double v = numero(campo, nome);
        if (v != Math.floor(v)) throw new IllegalArgumentException("\"" + nome + "\" deve ser um número inteiro.");
        return (int) v;
    }

    static String dinheiro(double v) { return String.format("%,.2f", v); }

    static void erro(Component pai, String msg) {
        JOptionPane.showMessageDialog(pai, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    static void info(Component pai, String msg) {
        JOptionPane.showMessageDialog(pai, msg, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
}
