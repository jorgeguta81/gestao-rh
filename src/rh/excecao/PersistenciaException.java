package rh.excecao;

/** Falha ao ler ou gravar os dados (disco cheio, ficheiro corrompido, sem permissões...). */
public class PersistenciaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensagem, Throwable causa) { super(mensagem, causa); }
    public PersistenciaException(String mensagem) { super(mensagem); }
}
