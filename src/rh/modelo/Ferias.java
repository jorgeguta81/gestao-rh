package rh.modelo;

import java.time.LocalDate;

public class Ferias implements Identificavel {
    private static final long serialVersionUID = 1L;

    private int id;
    private int funcionarioId;
    private LocalDate inicio;
    private LocalDate fim;
    private EstadoFerias estado = EstadoFerias.PENDENTE;
    private final int anoReferencia;
    private final int diasGozo;

    /**
     * @param anoReferencia ano a que o direito a férias se refere (pode gozar-se no próprio ano ou no seguinte)
     * @param diasGozo dias de calendário do período, sem os feriados (Lei 13/2023, art. 108.º e 111.º)
     */
    public Ferias(int funcionarioId, LocalDate inicio, LocalDate fim, int anoReferencia, int diasGozo) {
        this.funcionarioId = funcionarioId;
        this.inicio = inicio;
        this.fim = fim;
        this.anoReferencia = anoReferencia;
        this.diasGozo = diasGozo;
    }

    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }
    public int getFuncionarioId() { return funcionarioId; }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getFim() { return fim; }
    public EstadoFerias getEstado() { return estado; }
    public void setEstado(EstadoFerias estado) { this.estado = estado; }

    public int getAnoReferencia() { return anoReferencia; }

    /** Dias que contam para o direito a férias (dias de calendário menos feriados). */
    public int getDiasGozo() { return diasGozo; }

    @Override
    public String toString() {
        return String.format("#%d | Func: %d | %s a %s | %d dias | %s",
                id, funcionarioId, inicio, fim, diasGozo, estado);
    }
}
