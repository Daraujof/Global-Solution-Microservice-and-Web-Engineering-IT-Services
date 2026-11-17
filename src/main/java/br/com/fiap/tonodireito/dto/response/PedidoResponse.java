package br.com.fiap.tonodireito.dto.response;

import br.com.fiap.tonodireito.entity.enums.StatusPedido;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PedidoResponse {
    private Long id;
    private StatusPedido status;
    private LocalDate dataSolicitacao;
    private String protocolo;
    private String observacao;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private Long triagemId;
    private Long direitoId;
    private String direitoNome;
}
