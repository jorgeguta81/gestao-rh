package rh.excecao;

import java.util.List;

/** Os dados introduzidos não são válidos. Guarda todos os erros encontrados, para o utilizador corrigir de uma vez. */
public class ValidacaoException extends RhException {
    private static final long serialVersionUID = 1L;
    private final List<String> erros;

    public ValidacaoException(List<String> erros) {
        super(erros.size() == 1 ? erros.get(0) : "Corrija os seguintes erros:\n - " + String.join("\n - ", erros));
        this.erros = List.copyOf(erros);
    }

    public ValidacaoException(String erro) { this(List.of(erro)); }

    public List<String> getErros() { return erros; }
}
