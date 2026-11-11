package br.com.felipetrainer.areamembros.aluno;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    Optional<Compra> findByTransacao(String transacao);
}
