package br.com.fiap.tonodireito.dto.request;

import br.com.fiap.tonodireito.entity.enums.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Acompanhamento de um direito pedido a partir de uma triagem")
public class PedidoRequest {

    @NotNull(message = "O status é obrigatório")
    @Schema(example = "JA_PEDI")
    private StatusPedido status;

    @PastOrPresent(message = "A data da solicitação não pode estar no futuro")
    @Schema(example = "2026-11-10")
    private LocalDate dataSolicitacao;

    @Size(max = 60, message = "O protocolo deve ter no máximo 60 caracteres")
    @Schema(example = "INSS-2026-000123")
    private String protocolo;

    @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres")
    @Schema(example = "Pedido feito pelo Meu INSS com ajuda da assistente social do CRAS")
    private String observacao;

    @NotNull(message = "O id da triagem é obrigatório")
    @Positive(message = "O id da triagem deve ser positivo")
    @Schema(example = "1")
    private Long triagemId;

    @NotNull(message = "O id do direito é obrigatório")
    @Positive(message = "O id do direito deve ser positivo")
    @Schema(example = "1")
    private Long direitoId;
}
