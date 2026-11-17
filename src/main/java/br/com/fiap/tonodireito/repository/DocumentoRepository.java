package br.com.fiap.tonodireito.repository;

import br.com.fiap.tonodireito.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByDireitoId(Long direitoId);

    long countByDireitoId(Long direitoId);
}
