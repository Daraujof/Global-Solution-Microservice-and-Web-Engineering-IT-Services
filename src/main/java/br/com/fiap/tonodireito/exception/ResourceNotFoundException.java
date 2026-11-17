package br.com.fiap.tonodireito.exception;

/** Recurso inexistente -> 404 Not Found. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String recurso, Long id) {
        super(recurso + " com id " + id + " não encontrado(a)");
    }
}
