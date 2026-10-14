package rh.visao;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import rh.modelo.TipoFuncionario;
import rh.servico.RelatorioServico.Painel;
import rh.servico.RelatorioServico.Relatorio;
import rh.util.Formato;

/** TELA - Dashboard e relatórios (RH e Administrador): indicadores, gráficos, alertas e exportação CSV. */
public class DashboardFrame extends TelaBase {
    private final JLabel titulo = new JLabel(" ");
    private final JLabel kpiActivos = valor(), kpiBruto = valor(), kpiLiquido = valor(), kpiCusto = valor(), kpiFerias = valor();
    private final JLabel detalhe = new JLabel(" ");
    private final GraficoBarras grTipo = new GraficoBarras("Funcionários activos por tipo", v -> String.valueOf((int) v));
    private final GraficoBarras grDepHeadcount = new GraficoBarras("Funcionários activos por departamento", v -> String.valueOf((int) v));
    private final GraficoBarras grDepMassa = new GraficoBarras("Massa salarial bruta por departamento (MT)", Formato::dinheiro);
    private final JTextArea alertas = new JTextArea(5, 30);

    public DashboardFrame(Navegador nav) {
        super("Dashboard e relatórios", nav);

        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 12, 2, 12));
        detalhe.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.add(titulo, BorderLayout.NORTH);
        cabecalho.add(detalhe, BorderLayout.CENTER);

        JPanel cartoes = new JPanel(new GridLayout(1, 5, 10, 0));
        cartoes.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));
        cartoes.add(cartao("Funcionários activos", kpiActivos));
        cartoes.add(cartao("Massa salarial bruta (MT)", kpiBruto));
        cartoes.add(cartao("Líquido a pagar (MT)", kpiLiquido));
        cartoes.add(cartao("Custo para a empresa (MT)", kpiCusto));
        cartoes.add(cartao("Férias por decidir", kpiFerias));

        JPanel graficos = new JPanel(new GridLayout(1, 3, 12, 0));
        graficos.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        graficos.add(grTipo);
        graficos.add(grDepHeadcount);
        graficos.add(grDepMassa);

        alertas.setEditable(false);
        alertas.setLineWrap(true);
        alertas.setWrapStyleWord(true);
        alertas.setBackground(UIManager.getColor("Panel.background"));
        JScrollPane scrollAlertas = new JScrollPane(alertas);
        scrollAlertas.setBorder(BorderFactory.createTitledBorder("Alertas"));
        scrollAlertas.setPreferredSize(new Dimension(300, 110));

        JPanel exportar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        exportar.setBorder(BorderFactory.createTitledBorder("Exportar para Excel (CSV)"));
        exportar.add(botaoExportar("Folha de salários", Relatorio.FOLHA, "folha"));
        exportar.add(botaoExportar("Funcionários", Relatorio.FUNCIONARIOS, "funcionarios"));
        exportar.add(botaoExportar("Férias", Relatorio.FERIAS, "ferias"));

        JPanel sul = new JPanel(new BorderLayout());
        sul.add(scrollAlertas, BorderLayout.CENTER);
        JPanel direita = new JPanel(new BorderLayout());
        direita.add(exportar, BorderLayout.CENTER);
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bActualizar = new JButton("Actualizar");
        bActualizar.addActionListener(e -> atualizar());
        botoes.add(bActualizar);
        botoes.add(botaoVoltar());
        direita.add(botoes, BorderLayout.SOUTH);
        sul.add(direita, BorderLayout.EAST);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(cartoes, BorderLayout.NORTH);
        JPanel topoGraficos = new JPanel(new BorderLayout()); // gráficos encostados ao topo, sem scroll horizontal
        topoGraficos.add(graficos, BorderLayout.NORTH);
        centro.add(topoGraficos, BorderLayout.CENTER);

        add(cabecalho, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(sul, BorderLayout.SOUTH);
        atualizar();
        dimensionar(1150, 560);
    }

    private static JLabel valor() {
        JLabel l = new JLabel(" ", SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 20f));
        return l;
    }

    private static JPanel cartao(String texto, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        JLabel t = new JLabel(texto, SwingConstants.CENTER);
        p.add(t, BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

    private JButton botaoExportar(String texto, Relatorio relatorio, String prefixo) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> exportar(relatorio, prefixo));
        return b;
    }

    private void exportar(Relatorio relatorio, String prefixo) {
        Ui.tentar(this, () -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File(prefixo + "-" + LocalDate.now() + ".csv"));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            Path destino = fc.getSelectedFile().toPath();
            if (!destino.toString().toLowerCase().endsWith(".csv")) destino = Path.of(destino + ".csv");
            if (Files.exists(destino) && !Ui.confirmar(this, "O ficheiro já existe. Substituir?")) return;
            app.relatorios.exportar(relatorio, destino);
            Ui.info(this, "Relatório exportado para:\n" + destino
                    + "\n\nAbra no Excel (separador ';', codificação UTF-8). Os dados pessoais exportados são da sua responsabilidade.");
        });
    }

    @Override
    protected void atualizar() {
        Ui.tentar(this, () -> {
            Painel p = app.relatorios.painel();
            titulo.setText("Dashboard" + (p.empresa().isBlank() ? "" : " - " + p.empresa()));
            detalhe.setText(String.format(Formato.PT_MZ,
                    "<html>%s  |  %d funcionários no total (%d inactivos)  |  salário bruto médio: %s MT  |  %d admissão(ões) nos últimos 12 meses<br>"
                            + "Férias: %d pessoa(s) de férias hoje  |  %d aprovada(s) a começar nos próximos 30 dias</html>",
                    p.data(), p.totalFuncionarios(), p.inactivos(), Formato.dinheiro(p.salarioMedio()),
                    p.admissoesUltimos12Meses(), p.ferias().emFeriasHoje(), p.ferias().proximos30Dias()));
            kpiActivos.setText(String.valueOf(p.activos()));
            kpiBruto.setText(Formato.dinheiro(p.folha().bruto()));
            kpiLiquido.setText(Formato.dinheiro(p.folha().liquido()));
            kpiCusto.setText(Formato.dinheiro(p.folha().custoEmpresa()));
            kpiFerias.setText(String.valueOf(p.ferias().pendentes()));

            List<String> r = new ArrayList<>();
            List<Double> v = new ArrayList<>();
            for (TipoFuncionario t : TipoFuncionario.values()) { r.add(t.toString()); v.add((double) p.activosPorTipo().get(t)); }
            grTipo.definir(r, v);

            List<String> nomes = new ArrayList<>();
            List<Double> pessoas = new ArrayList<>(), massa = new ArrayList<>();
            p.departamentos().forEach(d -> { nomes.add(d.nome()); pessoas.add((double) d.activos()); massa.add(d.massaBruta()); });
            grDepHeadcount.definir(nomes, pessoas);
            grDepMassa.definir(nomes, massa);

            alertas.setText(p.alertas().isEmpty() ? "Sem alertas." : "• " + String.join("\n• ", p.alertas()));
        });
    }
}
