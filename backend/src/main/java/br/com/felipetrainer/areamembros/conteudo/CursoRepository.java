package br.com.felipetrainer.areamembros.conteudo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    List<Curso> findAllByOrderByOrdemAscIdAsc();
    List<Curso> findByPublicadoTrueOrderByOrdemAscIdAsc();
    Optional<Curso> findBySlug(String slug);
    Optional<Curso> findFirstByEduzzProdutoId(String eduzzProdutoId);
    boolean existsBySlug(String slug);
}
