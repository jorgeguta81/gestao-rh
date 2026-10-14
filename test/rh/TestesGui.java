package rh;

/**
 * Teste da interface gráfica (pesquisa e ordenação por cabeçalho). Precisa de ambiente gráfico:
 * no Linux sem ecrã use  xvfb-run -a java -cp /tmp/rh-out rh.TestesGui
 */
import java.awt.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import javax.swing.*;
import rh.visao.Navegador;
import java.awt.image.BufferedImage; import javax.imageio.ImageIO; import java.io.File;
public class TestesGui {
  static Object get(Object o, String n) throws Exception { Field f = o.getClass().getDeclaredField(n); f.setAccessible(true); return f.get(o); }
  static void ok(boolean c, String m){ System.out.println((c?"ok  - ":"FALHOU: ")+m); if(!c) System.exit(1); }
  public static void main(String[] x) throws Exception {
    Aplicacao app = new Aplicacao(Files.createTempDirectory("gp")); app.utilizadores.login("admin","Admin@123");
    Navegador n = new Navegador(app);
    final JFrame[] fr = new JFrame[1];
    SwingUtilities.invokeAndWait(() -> { n.abrir(Navegador.Tela.FUNCIONARIOS); for (Frame f : Frame.getFrames()) if (f.isDisplayable() && f.getTitle().equals("Funcionários")) fr[0]=(JFrame)f; });
    JTable t = (JTable) get(fr[0],"tabela"); JTextField busca = (JTextField) get(fr[0],"tfBusca"); JComboBox<?> estado = (JComboBox<?>) get(fr[0],"cbFiltroEstado");
    ok(t.getRowCount()==4, "4 linhas no início");
    SwingUtilities.invokeAndWait(() -> busca.setText("macuacua")); ok(t.getRowCount()==1 && t.getValueAt(0,2).equals("Ana Macuácua"), "escrever 'macuacua' filtra para Ana");
    SwingUtilities.invokeAndWait(() -> busca.setText("")); ok(t.getRowCount()==4, "limpar a pesquisa repõe as 4");
    // ordenar pelo cabeçalho de salário (coluna 8): numérico, não texto
    SwingUtilities.invokeAndWait(() -> t.getRowSorter().toggleSortOrder(8));
    ok(((Double)t.getValueAt(0,8))==8000.0 && ((Double)t.getValueAt(3,8))==75000.0, "cabeçalho Salário ordena numericamente (8.000 antes de 38.000)");
    SwingUtilities.invokeAndWait(() -> t.getRowSorter().toggleSortOrder(0)); SwingUtilities.invokeAndWait(() -> t.getRowSorter().toggleSortOrder(0));
    ok(((Integer)t.getValueAt(0,0))==4, "cabeçalho ID decrescente: 4 primeiro");
    // seleccionar linha preenche formulário
    SwingUtilities.invokeAndWait(() -> t.setRowSelectionInterval(0,0));
    ok(((JTextField)get(fr[0],"tfNome")).getText().equals("João Cossa"), "seleccionar linha preenche o formulário (após ordenar por cabeçalho)");
    SwingUtilities.invokeAndWait(() -> { try { ((JComboBox<Object>)estado).setSelectedIndex(2); } catch(Exception e){} });
    ok(t.getRowCount()==0, "filtro 'Inactivos' não encontra ninguém");
    SwingUtilities.invokeAndWait(() -> { try { ((JComboBox<Object>)estado).setSelectedIndex(0);} catch(Exception e){} });
    SwingUtilities.invokeAndWait(() -> { try { BufferedImage im = new BufferedImage(fr[0].getWidth(), fr[0].getHeight(), BufferedImage.TYPE_INT_RGB); fr[0].paint(im.getGraphics()); ImageIO.write(im,"png",new File(System.getProperty("java.io.tmpdir"), "rh_funcionarios.png")); } catch(Exception e){} });
    System.exit(0);
  } }
