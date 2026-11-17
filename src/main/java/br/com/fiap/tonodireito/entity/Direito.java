package br.com.fiap.tonodireito.entity;

import br.com.fiap.tonodireito.entity.enums.CategoriaDireito;
import br.com.fiap.tonodireito.entity.enums.EsferaGoverno;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Um direito social (ex.: BPC, Tarifa Social de Energia), com a lei/fonte oficial por trás.
 */
@Entity
@Table(name = "direitos")
@Getter
@Setter
@NoArgsConstructor
public class Direito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String nome;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CategoriaDireito categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EsferaGoverno esfera;

    @Column(name = "fonte_oficial_url", nullable = false, length = 500)
    private String fonteOficialUrl;

    @Column(name = "idade_minima")
    private Integer idadeMinima;

    @Column(name = "data_revisao")
    private LocalDate dataRevisao;

    @Column(nullable = false)
    private Boolean ativo = Boolean.TRUE;

    @OneToMany(mappedBy = "direito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Documento> documentos = new ArrayList<>();

    @OneToMany(mappedBy = "direito")
    private List<Pedido> pedidos = new ArrayList<>();
}
