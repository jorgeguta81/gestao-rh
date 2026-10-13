package rh.excecao;

/** O utilizador autenticado não tem permissão para a operação. */
public class AcessoNegadoException extends RhException {
    private static final long serialVersionUID = 1L;
    public AcessoNegadoException(String mensagem) { super(mensagem); }
}
