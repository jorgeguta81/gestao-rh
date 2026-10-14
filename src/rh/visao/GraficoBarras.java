package rh.visao;

import java.awt.*;
import java.util.List;
import java.util.function.DoubleFunction;
import javax.swing.JPanel;
import javax.swing.UIManager;

/** Gráfico de barras horizontais desenhado com Java2D (sem bibliotecas externas). */
class GraficoBarras extends JPanel {
    private static final Color COR = new Color(52, 120, 190);
    private static final int LINHA = 26;

    private final String titulo;
    private final DoubleFunction<String> formato;
    private List<String> rotulos = List.of();
    private List<Double> valores = List.of();

    GraficoBarras(String titulo, DoubleFunction<String> formato) {
        this.titulo = titulo;
        this.formato = formato;
        setOpaque(false);
        setPreferredSize(new Dimension(380, 60));
    }

    void definir(List<String> rotulos, List<Double> valores) {
        this.rotulos = rotulos;
        this.valores = valores;
        setPreferredSize(new Dimension(380, 44 + Math.max(1, rotulos.size()) * LINHA));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Color texto = UIManager.getColor("Label.foreground") == null ? Color.DARK_GRAY : UIManager.getColor("Label.foreground");

        g.setColor(texto);
        g.setFont(getFont().deriveFont(Font.BOLD, 13f));
        g.drawString(titulo, 4, 16);
        g.setFont(getFont().deriveFont(12f));
        FontMetrics fm = g.getFontMetrics();

        if (rotulos.isEmpty()) {
            g.drawString("Sem dados", 4, 40);
            g.dispose();
            return;
        }
        double max = valores.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        if (max <= 0) max = 1;
        int larguraRotulo = 0, larguraValor = 0;
        for (int i = 0; i < rotulos.size(); i++) {
            larguraRotulo = Math.max(larguraRotulo, fm.stringWidth(rotulos.get(i)));
            larguraValor = Math.max(larguraValor, fm.stringWidth(formato.apply(valores.get(i))));
        }
        larguraRotulo = Math.min(larguraRotulo, getWidth() / 3);
        int xBarra = larguraRotulo + 12;
        int maxBarra = Math.max(20, getWidth() - xBarra - larguraValor - 16);
        int y = 30;
        for (int i = 0; i < rotulos.size(); i++) {
            int largura = (int) Math.round(valores.get(i) / max * maxBarra);
            String rotulo = rotulos.get(i);
            while (fm.stringWidth(rotulo) > larguraRotulo && rotulo.length() > 3) rotulo = rotulo.substring(0, rotulo.length() - 2) + "…";
            g.setColor(texto);
            g.drawString(rotulo, 4, y + 14);
            g.setColor(COR);
            g.fillRoundRect(xBarra, y + 2, Math.max(largura, valores.get(i) > 0 ? 3 : 0), LINHA - 8, 6, 6);
            g.setColor(texto);
            g.drawString(formato.apply(valores.get(i)), xBarra + Math.max(largura, 3) + 6, y + 14);
            y += LINHA;
        }
        g.dispose();
    }
}
