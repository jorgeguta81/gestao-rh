package rh.visao;

import java.awt.*;
import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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

    /** Lê uma data AAAA-MM-DD de um campo de texto. */
    static LocalDate data(JTextField campo, String nome) {
        String t = campo.getText().trim();
        if (t.isEmpty()) throw new IllegalArgumentException("Preencha o campo \"" + nome + "\".");
        try {
            return LocalDate.parse(t);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("\"" + nome + "\": data inválida. Use AAAA-MM-DD, por exemplo 2026-12-15.");
        }
    }

    static boolean confirmar(Component pai, String msg) {
        return JOptionPane.showConfirmDialog(pai, msg, "Confirmar", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Devolve o ID (coluna 0) da linha seleccionada, ou -1 se não houver. */
    static int idSeleccionado(JTable tabela) {
        int r = tabela.getSelectedRow();
        return r < 0 ? -1 : (Integer) tabela.getValueAt(r, 0);
    }

    /** Volta a seleccionar a linha com este ID (depois de recarregar a tabela). */
    static void seleccionar(JTable tabela, int id) {
        for (int r = 0; r < tabela.getRowCount(); r++) {
            if ((Integer) tabela.getValueAt(r, 0) == id) { tabela.setRowSelectionInterval(r, r); return; }
        }
        tabela.clearSelection();
    }

    /** Define a largura preferida de cada coluna (em pixels). */
    static void larguras(JTable tabela, int... larguras) {
        for (int i = 0; i < larguras.length && i < tabela.getColumnCount(); i++)
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
    }
}
