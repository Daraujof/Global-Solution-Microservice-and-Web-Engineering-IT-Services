package br.com.fiap.tonodireito.dto.response;

import br.com.fiap.tonodireito.entity.enums.FaixaEtaria;
import br.com.fiap.tonodireito.entity.enums.FaixaRenda;
import br.com.fiap.tonodireito.entity.enums.RespostaTriagem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TriagemResponse {
    private Long id;
    private FaixaEtaria faixaEtaria;
    private FaixaRenda faixaRendaPerCapita;
    private RespostaTriagem inscritoCadUnico;
    private RespostaTriagem temDeficiencia;
    private Integer pessoasNaCasa;
    private String municipio;
    private String uf;
    private LocalDateTime criadoEm;
    private Long totalPedidos;
}
