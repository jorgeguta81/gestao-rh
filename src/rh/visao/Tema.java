package rh.visao;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.*;
import rh.util.Formato;

/**
 * Aspecto visual da aplicação: cores, tipo de letra, botões, tabelas e cabeçalhos.
 * Para mudar o "look" de TODA a aplicação basta alterar as cores/valores aqui.
 * Componentes que não devem ser tocados levam putClientProperty("tema.ignorar", true).
 */
public final class Tema {
    public static final Color AZUL = new Color(0x1F3A5F);
    public static final Color AZUL_MEDIO = new Color(0x2F6DB5);
    public static final Color FUNDO = new Color(0xF3F5F9);
    public static final Color CARTAO = Color.WHITE;
    public static final Color TEXTO = new Color(0x1E2A3A);
    public static final Color SUAVE = new Color(0x6B7A90);
    public static final Color BORDA = new Color(0xD5DBE5);
    public static final Color SELECAO = new Color(0xD6E6FA);
    public static final Color ZEBRA = new Color(0xF7F9FC);
    public static final Color VERDE = new Color(0x1E8E5A);
    public static final Color VERMELHO = new Color(0xC0392B);
    static final String IGNORAR = "tema.ignorar";

    private static boolean aplicado = false;
    private Tema() {}

    /** Liga o Look&Feel Nimbus com as cores e letra da aplicação (idempotente). */
    public static synchronized void aplicar() {
        if (aplicado) return;
        aplicado = true;
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                if ("Nimbus".equals(info.getName())) UIManager.setLookAndFeel(info.getClassName());
        } catch (Exception e) {
            // fica o aspecto por omissão do sistema
        }
        UIManager.put("control", FUNDO);
        UIManager.put("nimbusFocus", AZUL_MEDIO);
        UIManager.put("nimbusSelectionBackground", AZUL_MEDIO);
        UIManager.put("text", TEXTO);
        UIManager.put("defaultFont", letra(Font.PLAIN, 13));
        UIManager.put("OptionPane.background", FUNDO);
        UIManager.put("Panel.background", FUNDO);
    }

    /** Tipo de letra moderno se existir no sistema (Segoe UI no Windows), senão o genérico. */
    public static Font letra(int estilo, float tamanho) {
        String[] preferidas = {"Segoe UI", "Inter", "SF Pro Text", "Helvetica Neue", "Noto Sans", "Dialog"};
        java.util.Set<String> existentes = new java.util.HashSet<>(
                List.of(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : preferidas) if (existentes.contains(f)) return new Font(f, estilo, 1).deriveFont(tamanho);
        return new Font(Font.SANS_SERIF, estilo, 1).deriveFont(tamanho);
    }

    // ------------------------------------------------------------------ componentes prontos
    /** Faixa superior azul com título, subtítulo e um texto à direita. */
    public static JPanel banner(String titulo, String subtitulo, String direita) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, AZUL, getWidth(), 0, AZUL_MEDIO));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0xF2B705)); // linha de realce
                g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                g2.dispose();
            }
        };
        p.putClientProperty(IGNORAR, true);
        p.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        JPanel esq = new JPanel(new GridLayout(0, 1));
        esq.setOpaque(false);
        JLabel t = new JLabel(titulo);
        t.setForeground(Color.WHITE);
        t.setFont(letra(Font.BOLD, 20));
        esq.add(t);
        if (subtitulo != null && !subtitulo.isBlank()) {
            JLabel s = new JLabel(subtitulo);
            s.setForeground(new Color(0xC9D8EE));
            s.setFont(letra(Font.PLAIN, 12));
            esq.add(s);
        }
        p.add(esq, BorderLayout.WEST);
        if (direita != null && !direita.isBlank()) {
            JLabel d = new JLabel(direita);
            d.setForeground(new Color(0xE6EEF9));
            d.setFont(letra(Font.PLAIN, 12));
            p.add(d, BorderLayout.EAST);
        }
        return p;
    }

    // ------------------------------------------------------------------ estilização automática
    /** Percorre a árvore de componentes e aplica o estilo (botões, tabelas, campos, painéis com título). */
    public static void estilizar(Component c) {
        boolean ignorar = c instanceof JComponent jc && Boolean.TRUE.equals(jc.getClientProperty(IGNORAR));
        if (ignorar && c instanceof AbstractButton) return;          // cartões do menu: desenho próprio
        if (!ignorar) {                                               // "ignorar" = não mexer neste componente, mas sim nos filhos
            if (c instanceof JButton b) estilizarBotao(b);
            else if (c instanceof JTable t) estilizarTabela(t);
            else if (c instanceof JTextField tf) estilizarCampo(tf);
            else if (c instanceof JScrollPane sp) {
                sp.setBorder(BorderFactory.createLineBorder(BORDA));
                sp.getViewport().setBackground(CARTAO);
            } else if (c instanceof JPanel p) {
                if (p.getBorder() instanceof TitledBorder tb) {
                    tb.setBorder(BorderFactory.createLineBorder(BORDA));
                    tb.setTitleFont(letra(Font.BOLD, 12));
                    tb.setTitleColor(AZUL);
                    p.setOpaque(true);
                    p.setBackground(CARTAO);
                } else {
                    p.setOpaque(false);
                }
            }
        }
        // componentes compostos (a seta de um combo é um JButton interno): não mexer nas peças internas
        if (c instanceof JComboBox || c instanceof JSpinner || c instanceof JScrollBar || c instanceof JTableHeader) return;
        if (c instanceof Container ct) for (Component filho : ct.getComponents()) estilizar(filho);
    }

    private static void estilizarCampo(JTextField tf) {
        tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDA),
                BorderFactory.createEmptyBorder(4, 7, 4, 7)));
        tf.setBackground(Color.WHITE);
    }

    // ---------- botões
    enum Estilo { PRIMARIO, PERIGO, SECUNDARIO }

    private static final java.util.Set<String> PRIMARIOS = java.util.Set.of(
            "Admitir", "Guardar", "Criar", "Solicitar", "Aprovar", "Entrar", "Ver recibo");
    private static final java.util.Set<String> PERIGOS = java.util.Set.of(
            "Remover", "Demitir", "Rejeitar", "Cancelar pedido");

    static void estilizarBotao(JButton b) {
        Estilo e = PRIMARIOS.contains(b.getText()) ? Estilo.PRIMARIO
                : PERIGOS.contains(b.getText()) ? Estilo.PERIGO : Estilo.SECUNDARIO;
        b.setUI(new BotaoUI(e));
        b.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setRolloverEnabled(true);
        b.setFont(letra(Font.BOLD, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Botão arredondado com estados (normal, por cima, premido, desactivado). */
    private static final class BotaoUI extends BasicButtonUI {
        private final Estilo estilo;
        BotaoUI(Estilo estilo) { this.estilo = estilo; }

        @Override public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            ButtonModel m = b.getModel();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Color base = switch (estilo) { case PRIMARIO -> AZUL_MEDIO; case PERIGO -> VERMELHO; case SECUNDARIO -> Color.WHITE; };
            Color fundo = !b.isEnabled() ? new Color(0xE3E7EE)
                    : m.isPressed() ? (estilo == Estilo.SECUNDARIO ? SELECAO : base.darker())
                    : m.isRollover() ? (estilo == Estilo.SECUNDARIO ? new Color(0xEEF3FA) : base.brighter())
                    : base;
            g2.setColor(fundo);
            g2.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 10, 10);
            if (estilo == Estilo.SECUNDARIO || !b.isEnabled()) {
                g2.setColor(BORDA);
                g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 10, 10);
            }
            if (b.isFocusOwner()) {
                g2.setColor(new Color(0x2F6DB5));
                g2.drawRoundRect(1, 1, c.getWidth() - 3, c.getHeight() - 3, 9, 9);
            }
            g2.setFont(b.getFont());
            FontMetrics fm = g2.getFontMetrics();
            String t = b.getText() == null ? "" : b.getText();
            g2.setColor(!b.isEnabled() ? new Color(0x9AA5B5) : estilo == Estilo.SECUNDARIO ? TEXTO : Color.WHITE);
            g2.drawString(t, (c.getWidth() - fm.stringWidth(t)) / 2, (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // ---------- tabelas
    static void estilizarTabela(JTable t) {
        t.setRowHeight(28);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(SELECAO);
        t.setSelectionForeground(TEXTO);
        t.setFont(letra(Font.PLAIN, 13));
        CelulaRenderer r = new CelulaRenderer();
        for (Class<?> k : new Class<?>[]{Object.class, Number.class, Integer.class, Double.class, LocalDate.class})
            t.setDefaultRenderer(k, r);
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(h.getPreferredSize().width, 32));
        h.setDefaultRenderer(new CabecalhoRenderer());
    }

    private static final class CelulaRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int l, int c) {
            super.getTableCellRendererComponent(t, v instanceof Double d ? Formato.dinheiro(d) : v, sel, false, l, c);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setHorizontalAlignment(v instanceof Number ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setForeground(TEXTO);
            setBackground(sel ? SELECAO : (l % 2 == 0 ? CARTAO : ZEBRA));
            if (v instanceof String || v instanceof Enum<?>) {
                String s = String.valueOf(v);
                if (s.equals("Inactivo") || s.equals("REJEITADA") || s.equals("Inactiva") || s.equals("Bloqueada")) setForeground(VERMELHO);
                else if (s.equals("Activo") || s.equals("APROVADA") || s.equals("Activa")) setForeground(VERDE);
                else if (s.equals("PENDENTE")) setForeground(new Color(0xB7791F));
            }
            return this;
        }
    }

    private static final class CabecalhoRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int l, int c) {
            String texto = String.valueOf(v);
            RowSorter<?> rs = t.getRowSorter();
            if (rs != null && !rs.getSortKeys().isEmpty() && rs.getSortKeys().get(0).getColumn() == t.convertColumnIndexToModel(c))
                texto += rs.getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING ? "  \u25B2" : "  \u25BC";
            super.getTableCellRendererComponent(t, texto, false, false, l, c);
            setBackground(AZUL);
            setForeground(Color.WHITE);
            setFont(letra(Font.BOLD, 12));
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setHorizontalAlignment(SwingConstants.LEFT);
            return this;
        }
    }

    // ------------------------------------------------------------------ cartão clicável (menu)
    /** Botão-cartão do menu principal: fundo branco, faixa de cor, letra-emblema, título e descrição. */
    static JButton cartaoMenu(String titulo, String descricao, Color cor, Runnable acao) {
        JButton b = new JButton() {
            private boolean sobre;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { sobre = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { sobre = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(sobre || isFocusOwner() ? new Color(0xEEF4FC) : CARTAO);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
                g2.setColor(sobre || isFocusOwner() ? cor : BORDA);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
                g2.setColor(cor);
                g2.fillRoundRect(0, 0, 6, h - 1, 6, 6);
                int d = 46;
                g2.fillOval(22, (h - d) / 2, d, d);
                g2.setColor(Color.WHITE);
                g2.setFont(letra(Font.BOLD, 22));
                String ini = titulo.substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ini, 22 + (d - fm.stringWidth(ini)) / 2, (h - d) / 2 + (d - fm.getHeight()) / 2 + fm.getAscent());
                g2.setColor(TEXTO);
                g2.setFont(letra(Font.BOLD, 15));
                g2.drawString(titulo, 82, h / 2 - 2);
                g2.setColor(SUAVE);
                g2.setFont(letra(Font.PLAIN, 12));
                g2.drawString(descricao, 82, h / 2 + 18);
                g2.dispose();
            }
        };
        b.putClientProperty(IGNORAR, true);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(270, 96));
        b.getAccessibleContext().setAccessibleName(titulo + ". " + descricao);
        b.addActionListener(e -> acao.run());
        return b;
    }
}
