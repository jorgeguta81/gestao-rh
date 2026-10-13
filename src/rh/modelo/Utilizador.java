package rh.modelo;

import java.time.LocalDateTime;

/** Conta de acesso à aplicação. Nunca guarda a palavra-passe, só o sal e o hash. */
public class Utilizador implements Identificavel {
    private static final long serialVersionUID = 1L;
    public static final int SEM_FUNCIONARIO = 0;

    private int id;
    private String nomeUtilizador;
    private String sal;
    private String hash;
    private Perfil perfil;
    private int funcionarioId = SEM_FUNCIONARIO;
    private boolean ativo = true;
    private boolean mudarPassword = true;
    private int tentativasFalhadas;
    private LocalDateTime ultimoAcesso;

    public Utilizador(String nomeUtilizador, String sal, String hash, Perfil perfil, int funcionarioId) {
        this.nomeUtilizador = nomeUtilizador;
        this.sal = sal;
        this.hash = hash;
        this.perfil = perfil;
        this.funcionarioId = funcionarioId;
    }

    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }
    public String getNomeUtilizador() { return nomeUtilizador; }
    public String getSal() { return sal; }
    public String getHash() { return hash; }
    public void definirPassword(String sal, String hash) { this.sal = sal; this.hash = hash; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public int getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(int funcionarioId) { this.funcionarioId = funcionarioId; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public boolean isMudarPassword() { return mudarPassword; }
    public void setMudarPassword(boolean mudarPassword) { this.mudarPassword = mudarPassword; }
    public int getTentativasFalhadas() { return tentativasFalhadas; }
    public void setTentativasFalhadas(int n) { this.tentativasFalhadas = n; }
    public LocalDateTime getUltimoAcesso() { return ultimoAcesso; }
    public void setUltimoAcesso(LocalDateTime t) { this.ultimoAcesso = t; }
}
