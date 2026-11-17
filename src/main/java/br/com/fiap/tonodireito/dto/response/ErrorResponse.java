package br.com.fiap.tonodireito.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

/** Corpo padronizado de erro: nunca expõe stack trace, SQL ou detalhes internos. */
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private OffsetDateTime timestamp;
    private int status;
    private String erro;
    private String mensagem;
    private String caminho;
    private List<CampoInvalido> campos;

    public record CampoInvalido(String campo, String mensagem) {
    }
}
