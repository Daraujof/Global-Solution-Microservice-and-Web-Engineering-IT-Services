package br.com.fiap.tonodireito.dto.request;

import br.com.fiap.tonodireito.entity.enums.FaixaEtaria;
import br.com.fiap.tonodireito.entity.enums.FaixaRenda;
import br.com.fiap.tonodireito.entity.enums.RespostaTriagem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Respostas anônimas de uma triagem (sem nome, CPF, idade ou renda exatas)")
public class TriagemRequest {

    @NotNull(message = "A faixa etária é obrigatória")
    @Schema(example = "DE_65_A_79")
    private FaixaEtaria faixaEtaria;

    @NotNull(message = "A faixa de renda por pessoa é obrigatória")
    @Schema(example = "ATE_1_4_SM")
    private FaixaRenda faixaRendaPerCapita;

    @NotNull(message = "Informe se a família está no CadÚnico (SIM, NAO ou NAO_SEI)")
    @Schema(example = "NAO")
    private RespostaTriagem inscritoCadUnico;

    @Schema(example = "NAO_SEI")
    private RespostaTriagem temDeficiencia;

    @Min(value = 1, message = "A casa deve ter pelo menos 1 pessoa")
    @Max(value = 30, message = "Número de pessoas na casa deve ser no máximo 30")
    @Schema(example = "2")
    private Integer pessoasNaCasa;

    @Size(max = 100, message = "O município deve ter no máximo 100 caracteres")
    @Schema(example = "São Paulo")
    private String municipio;

    @Pattern(regexp = "^[A-Za-z]{2}$", message = "A UF deve ter 2 letras")
    @Schema(example = "SP")
    private String uf;
}
