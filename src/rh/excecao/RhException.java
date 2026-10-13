package rh.excecao;

/** Base das excepções de negócio da aplicação (a mensagem já é amigável para mostrar ao utilizador). */
public class RhException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public RhException(String mensagem) { super(mensagem); }
    public RhException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
