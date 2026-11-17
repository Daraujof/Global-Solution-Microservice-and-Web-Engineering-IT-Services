package br.com.fiap.tonodireito.dto.request;

import br.com.fiap.tonodireito.entity.enums.CategoriaDireito;
import br.com.fiap.tonodireito.entity.enums.EsferaGoverno;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Dados para cadastrar/atualizar um direito social")
public class DireitoRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
    @Schema(example = "BPC - 1 salário mínimo por mês")
    private String nome;

    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
    @Schema(example = "Um salário mínimo por mês, pago pelo INSS, para pessoa idosa (65+) ou com deficiência de família com renda baixa. Não precisa ter contribuído.")
    private String descricao;

    @NotNull(message = "A categoria é obrigatória")
    @Schema(example = "DINHEIRO_BENEFICIO")
    private CategoriaDireito categoria;

    @NotNull(message = "A esfera de governo é obrigatória")
    @Schema(example = "FEDERAL")
    private EsferaGoverno esfera;

    @NotBlank(message = "A fonte oficial (lei/site do governo) é obrigatória")
    @Size(max = 500, message = "A URL da fonte deve ter no máximo 500 caracteres")
    @Pattern(regexp = "^https?://.+", message = "A fonte oficial deve ser uma URL http(s)")
    @Schema(example = "https://www.planalto.gov.br/ccivil_03/leis/l8742.htm")
    private String fonteOficialUrl;

    @Min(value = 0, message = "A idade mínima não pode ser negativa")
    @Max(value = 120, message = "A idade mínima deve ser no máximo 120")
    @Schema(example = "65")
    private Integer idadeMinima;

    @PastOrPresent(message = "A data de revisão não pode estar no futuro")
    @Schema(example = "2026-11-05")
    private LocalDate dataRevisao;

    @Schema(example = "true", description = "Se omitido, o direito é criado como ativo")
    private Boolean ativo;
}
