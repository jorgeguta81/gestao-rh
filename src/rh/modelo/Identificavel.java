package rh.modelo;

/** Interface: tudo o que é guardado num repositório tem um id. */
public interface Identificavel extends java.io.Serializable {
    int getId();
    void setId(int id);
}
