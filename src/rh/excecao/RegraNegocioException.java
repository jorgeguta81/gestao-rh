package rh.excecao;

/** Uma regra de negócio impede a operação (ex.: remover departamento com funcionários, férias a mais). */
public class RegraNegocioException extends RhException {
    private static final long serialVersionUID = 1L;
    public RegraNegocioException(String mensagem) { super(mensagem); }
}
