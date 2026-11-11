package br.com.felipetrainer.areamembros.conteudo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuloRepository extends JpaRepository<Modulo, Long> {
    Optional<Modulo> findByCursoIdAndSlug(Long cursoId, String slug);
    boolean existsByCursoIdAndSlug(Long cursoId, String slug);
}
