package br.com.fiap.tonodireito.entity;

import br.com.fiap.tonodireito.entity.enums.StatusPedido;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Acompanhamento de um direito pedido a partir de uma triagem
 * (Quero pedir -> Já pedi -> Aguardando -> Consegui / Negado).
 */
@Entity
@Table(name = "pedidos",
        uniqueConstraints = @UniqueConstraint(name = "uk_pedidos_triagem_direito",
                columnNames = {"triagem_id", "direito_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status;

    @Column(name = "data_solicitacao")
    private LocalDate dataSolicitacao;

    @Column(length = 60)
    private String protocolo;

    @Column(length = 500)
    private String observacao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "triagem_id", nullable = false)
    private Triagem triagem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "direito_id", nullable = false)
    private Direito direito;

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = this.criadoEm;
    }

    @PreUpdate
    void aoAtualizar() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
