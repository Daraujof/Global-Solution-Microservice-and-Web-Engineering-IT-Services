package br.com.fiap.tonodireito.dto.response;

import br.com.fiap.tonodireito.entity.enums.CategoriaDireito;
import br.com.fiap.tonodireito.entity.enums.EsferaGoverno;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DireitoResponse {
    private Long id;
    private String nome;
    private String descricao;
    private CategoriaDireito categoria;
    private EsferaGoverno esfera;
    private String fonteOficialUrl;
    private Integer idadeMinima;
    private LocalDate dataRevisao;
    private Boolean ativo;
    private Long totalDocumentos;
}
