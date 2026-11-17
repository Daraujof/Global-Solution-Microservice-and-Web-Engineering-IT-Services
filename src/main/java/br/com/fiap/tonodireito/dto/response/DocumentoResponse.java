package br.com.fiap.tonodireito.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DocumentoResponse {
    private Long id;
    private String nome;
    private String descricao;
    private Boolean obrigatorio;
    private String ondeEmitir;
    private Integer validadeMeses;
    private Long direitoId;
    private String direitoNome;
}
