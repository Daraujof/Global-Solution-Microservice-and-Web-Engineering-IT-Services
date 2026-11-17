package br.com.fiap.tonodireito.repository;

import br.com.fiap.tonodireito.entity.Direito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DireitoRepository extends JpaRepository<Direito, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
