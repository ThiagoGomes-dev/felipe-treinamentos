package br.com.felipetrainer.areamembros;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:teste;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH", "app.armazenamento.pasta=target/uploads-teste"})
class AreaMembrosApplicationTests {

    @Test
    void sobeOContexto() {
    }
}
