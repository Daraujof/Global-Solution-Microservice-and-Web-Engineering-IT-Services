package br.com.fiap.tonodireito.exception;

/** Conflito com o estado atual dos dados (duplicidade, vínculos) -> 409 Conflict. */
public class ConflictException extends RuntimeException {

    public ConflictException(String mensagem) {
        super(mensagem);
    }
}
