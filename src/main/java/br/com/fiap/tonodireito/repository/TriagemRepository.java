package br.com.fiap.tonodireito.repository;

import br.com.fiap.tonodireito.entity.Triagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TriagemRepository extends JpaRepository<Triagem, Long> {
}
