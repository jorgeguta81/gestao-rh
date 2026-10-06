package rh.modelo;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class Ferias implements Identificavel {
    private int id;
    private int funcionarioId;
    private LocalDate inicio;
    private LocalDate fim;
    private EstadoFerias estado = EstadoFerias.PENDENTE;

    public Ferias(int funcionarioId, LocalDate inicio, LocalDate fim) {
        this.funcionarioId = funcionarioId;
        this.inicio = inicio;
        this.fim = fim;
    }

    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }
    public int getFuncionarioId() { return funcionarioId; }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getFim() { return fim; }
    public EstadoFerias getEstado() { return estado; }
    public void setEstado(EstadoFerias estado) { this.estado = estado; }

    /** Dias úteis (seg-sex) entre início e fim, inclusive. */
    public int diasUteis() {
        int dias = 0;
        for (LocalDate d = inicio; !d.isAfter(fim); d = d.plusDays(1)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) dias++;
        }
        return dias;
    }

    @Override
    public String toString() {
        return String.format("#%d | Func: %d | %s a %s | %d dias úteis | %s",
                id, funcionarioId, inicio, fim, diasUteis(), estado);
    }
}
