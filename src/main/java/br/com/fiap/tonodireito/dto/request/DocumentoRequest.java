package br.com.fiap.tonodireito.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Documento necessário para pedir um direito")
public class DocumentoRequest {

    @NotBlank(message = "O nome do documento é obrigatório")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
    @Schema(example = "Documento com foto e CPF")
    private String nome;

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    @Schema(example = "RG, CNH ou carteira de trabalho, de todas as pessoas da casa")
    private String descricao;

    @NotNull(message = "Informe se o documento é obrigatório")
    @Schema(example = "true")
    private Boolean obrigatorio;

    @Size(max = 300, message = "O local de emissão deve ter no máximo 300 caracteres")
    @Schema(example = "Poupatempo ou CRAS do bairro")
    private String ondeEmitir;

    @Positive(message = "A validade em meses deve ser positiva")
    @Schema(example = "24")
    private Integer validadeMeses;

    @NotNull(message = "O id do direito é obrigatório")
    @Positive(message = "O id do direito deve ser positivo")
    @Schema(example = "1")
    private Long direitoId;
}
