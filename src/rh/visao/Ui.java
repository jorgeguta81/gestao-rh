package rh.visao;

import java.awt.*;
import javax.swing.*;
import java.time.LocalDate;
import rh.excecao.*;
import rh.util.Registo;
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

    /**
     * Modelo só de leitura. O tipo de cada coluna vem do primeiro valor (Integer, Double, LocalDate...),
     * por isso clicar no cabeçalho ordena números e datas correctamente (e não como texto).
     */
    static DefaultTableModel modeloTabela(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                for (int r = 0; r < getRowCount(); r++) {
                    Object v = getValueAt(r, c);
                    if (v != null) return v.getClass();
                }
                return Object.class;
            }
        };
    }

    /** Liga a ordenação por clique no cabeçalho e formata os valores monetários (Double) com 2 casas e alinhados à direita. */
    static JTable configurar(JTable tabela) {
        tabela.setAutoCreateRowSorter(true);
        tabela.setDefaultRenderer(Double.class, new javax.swing.table.DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.RIGHT); }
            @Override protected void setValue(Object v) { setText(v instanceof Double d ? dinheiro(d) : String.valueOf(v)); }
        });
        return tabela;
    }

    /** Mostra o nome de um Departamento numa lista; outros itens (ex.: "Todos") aparecem como texto. */
    static ListCellRenderer<Object> rendererDepartamento() {
        return new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof rh.modelo.Departamento d) setText(d.getNome());
                return this;
            }
        };
    }

    /** Painel de filtros com duas linhas (não transborda em janelas estreitas). */
    static JPanel painelFiltros(String titulo, JComponent[] linha1, JComponent[] linha2) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBorder(BorderFactory.createTitledBorder(titulo));
        for (JComponent[] linha : new JComponent[][]{linha1, linha2}) {
            JPanel l = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            for (JComponent c : linha) l.add(c);
            p.add(l);
        }
        return p;
    }

    /** Executa a acção sempre que o texto do campo muda (pesquisa enquanto se escreve). */
    static void aoEscrever(JTextField campo, Runnable acao) {
        campo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { acao.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { acao.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { acao.run(); }
        });
    }

    static double numero(JTextField campo, String nome) {
        String t = campo.getText().trim().replace(',', '.');
        if (t.isEmpty()) throw new ValidacaoException("Preencha o campo \"" + nome + "\".");
        try {
            double v = Double.parseDouble(t);
            if (!Double.isFinite(v)) throw new ValidacaoException("\"" + nome + "\" deve ser um número.");
            if (v < 0) throw new ValidacaoException("\"" + nome + "\" não pode ser negativo.");
            return v;
        } catch (NumberFormatException e) {
            throw new ValidacaoException("\"" + nome + "\" deve ser um número.");
        }
    }

    static int inteiro(JTextField campo, String nome) {
        double v = numero(campo, nome);
        if (v != Math.floor(v)) throw new ValidacaoException("\"" + nome + "\" deve ser um número inteiro.");
        return (int) v;
    }

    static String dinheiro(double v) { return String.format("%,.2f", v); }

    static void erro(Component pai, String msg) {
        JOptionPane.showMessageDialog(pai, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Executa uma acção da tela e trata as excepções num só sítio:
     * erros esperados (validação, regras, acesso) viram avisos claros; erros inesperados são
     * registados no ficheiro de log e o utilizador vê uma mensagem genérica (nunca um stack trace).
     */
    static void tentar(Component pai, Runnable acao) {
        try {
            acao.run();
        } catch (ValidacaoException e) {
            JOptionPane.showMessageDialog(pai, e.getMessage(), "Dados inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (AcessoNegadoException e) {
            JOptionPane.showMessageDialog(pai, e.getMessage(), "Acesso negado", JOptionPane.WARNING_MESSAGE);
        } catch (PersistenciaException e) {
            Registo.erro("Falha de persistência", e);
            JOptionPane.showMessageDialog(pai, e.getMessage() + "\nA operação não foi concluída.\nDetalhes em: "
                    + Registo.ficheiroDeErros(), "Erro ao gravar/ler dados", JOptionPane.ERROR_MESSAGE);
        } catch (RhException e) {
            JOptionPane.showMessageDialog(pai, e.getMessage(), "Operação não permitida", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException e) {
            mostrarErroInesperado(pai, e);
        }
    }

    /** Erro inesperado (bug): regista o detalhe e mostra mensagem simples. */
    static void mostrarErroInesperado(Component pai, Throwable e) {
        Registo.erro("Erro inesperado na interface", e);
        JOptionPane.showMessageDialog(pai, "Ocorreu um erro inesperado e a operação foi cancelada.\n"
                + "Os detalhes técnicos foram guardados em:\n" + Registo.ficheiroDeErros(),
                "Erro inesperado", JOptionPane.ERROR_MESSAGE);
    }

    static void info(Component pai, String msg) {
        JOptionPane.showMessageDialog(pai, msg, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Lê uma data AAAA-MM-DD de um campo de texto. */
    static LocalDate data(JTextField campo, String nome) {
        String t = campo.getText().trim();
        if (t.isEmpty()) throw new ValidacaoException("Preencha o campo \"" + nome + "\".");
        try {
            return LocalDate.parse(t);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("\"" + nome + "\": data inválida. Use AAAA-MM-DD, por exemplo 2026-12-15.");
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
