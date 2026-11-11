package br.com.felipetrainer.areamembros.conteudo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<Site, Long> {
    Optional<Site> findFirstByOrderByIdAsc();
}
