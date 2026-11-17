package br.com.fiap.tonodireito.exception;

/** Violação de regra de negócio com dados enviados pelo cliente -> 400 Bad Request. */
public class BusinessException extends RuntimeException {

    public BusinessException(String mensagem) {
        super(mensagem);
    }
}
