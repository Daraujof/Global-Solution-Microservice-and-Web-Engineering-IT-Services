package br.com.fiap.tonodireito.entity;

import br.com.fiap.tonodireito.entity.enums.FaixaEtaria;
import br.com.fiap.tonodireito.entity.enums.FaixaRenda;
import br.com.fiap.tonodireito.entity.enums.RespostaTriagem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro ANÔNIMO das respostas de uma triagem (sem nome, sem CPF, sem idade ou renda exatas).
 * "Regras no banco. Pessoas, nunca."
 */
@Entity
@Table(name = "triagens")
@Getter
@Setter
@NoArgsConstructor
public class Triagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "faixa_etaria", nullable = false, length = 20)
    private FaixaEtaria faixaEtaria;

    @Enumerated(EnumType.STRING)
    @Column(name = "faixa_renda_per_capita", nullable = false, length = 20)
    private FaixaRenda faixaRendaPerCapita;

    @Enumerated(EnumType.STRING)
    @Column(name = "inscrito_cadunico", nullable = false, length = 10)
    private RespostaTriagem inscritoCadUnico;

    @Enumerated(EnumType.STRING)
    @Column(name = "tem_deficiencia", length = 10)
    private RespostaTriagem temDeficiencia;

    @Column(name = "pessoas_na_casa")
    private Integer pessoasNaCasa;

    @Column(length = 100)
    private String municipio;

    @Column(length = 2)
    private String uf;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @OneToMany(mappedBy = "triagem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pedido> pedidos = new ArrayList<>();

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }
}
