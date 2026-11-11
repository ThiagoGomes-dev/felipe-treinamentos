package br.com.felipetrainer.areamembros.aluno;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    Optional<Matricula> findByUsuarioIdAndCursoId(Long usuarioId, Long cursoId);

    List<Matricula> findByUsuarioId(Long usuarioId);

    long countByCursoIdAndAtivaTrue(Long cursoId);

    boolean existsByUsuarioIdAndCursoIdAndAtivaTrue(Long usuarioId, Long cursoId);

    @Query("select m.curso.id from Matricula m where m.usuario.id = :usuarioId and m.ativa = true")
    Set<Long> cursosLiberados(Long usuarioId);
}
