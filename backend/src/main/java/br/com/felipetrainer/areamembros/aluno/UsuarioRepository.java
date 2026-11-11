package br.com.felipetrainer.areamembros.aluno;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailIgnoreCase(String email);
    boolean existsByPapel(Papel papel);
    long countByPapelAndAtivoTrue(Papel papel);
    List<Usuario> findAllByOrderByCriadoEmDesc();
}
