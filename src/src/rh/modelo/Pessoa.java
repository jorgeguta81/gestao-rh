package rh.modelo;

/** Classe base abstracta (herança). */
public abstract class Pessoa implements Identificavel {
    private int id;
    private String nome;
    private String bi;
    private String email;
    private String telefone;

    protected Pessoa(String nome, String bi, String email, String telefone) {
        this.nome = nome;
        this.bi = bi;
        this.email = email;
        this.telefone = telefone;
    }

    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getBi() { return bi; }
    public void setBi(String bi) { this.bi = bi; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public abstract String getTipo();

    @Override
    public String toString() {
        return String.format("#%d | %-10s | %s | BI: %s | %s | %s",
                id, getTipo(), nome, bi, email, telefone);
    }
}
