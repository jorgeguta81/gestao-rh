package rh.excecao;

/** Credenciais inválidas, conta bloqueada ou inactiva. */
public class AutenticacaoException extends RhException {
    private static final long serialVersionUID = 1L;
    public AutenticacaoException(String mensagem) { super(mensagem); }
}
