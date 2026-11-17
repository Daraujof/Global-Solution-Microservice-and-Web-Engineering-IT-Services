package br.com.fiap.tonodireito.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Documento que a pessoa precisa levar para pedir um direito ("o que levar").
 */
@Entity
@Table(name = "documentos")
@Getter
@Setter
@NoArgsConstructor
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private Boolean obrigatorio = Boolean.TRUE;

    @Column(name = "onde_emitir", length = 300)
    private String ondeEmitir;

    @Column(name = "validade_meses")
    private Integer validadeMeses;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "direito_id", nullable = false)
    private Direito direito;
}
