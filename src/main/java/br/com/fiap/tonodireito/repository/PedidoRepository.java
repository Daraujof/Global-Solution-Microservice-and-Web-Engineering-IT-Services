package br.com.fiap.tonodireito.repository;

import br.com.fiap.tonodireito.entity.Pedido;
import br.com.fiap.tonodireito.entity.enums.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByStatus(StatusPedido status);

    List<Pedido> findByTriagemId(Long triagemId);

    boolean existsByDireitoId(Long direitoId);

    boolean existsByTriagemIdAndDireitoId(Long triagemId, Long direitoId);

    boolean existsByTriagemIdAndDireitoIdAndIdNot(Long triagemId, Long direitoId, Long id);

    long countByTriagemId(Long triagemId);
}
