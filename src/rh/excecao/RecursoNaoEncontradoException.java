package rh.excecao;

/** O funcionário, departamento, pedido ou utilizador pedido não existe (ex.: já foi removido). */
public class RecursoNaoEncontradoException extends RhException {
    private static final long serialVersionUID = 1L;
    public RecursoNaoEncontradoException(String mensagem) { super(mensagem); }
}
