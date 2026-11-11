package br.com.felipetrainer.areamembros.eduzz;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.felipetrainer.areamembros.aluno.SenhaProvisoria;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EduzzEventoTest {

    @Test
    void formularioAntigoPago() {
        var ev = EduzzEvento.deFormulario(Map.of(
                "trans_cod", "123", "trans_status", "3", "product_cod", "777",
                "cus_name", "Ana", "cus_email", "ana@ex.com", "cus_birth_date", "15/03/1990"));
        assertThat(ev.aprovada()).isTrue();
        assertThat(ev.cancelada()).isFalse();
        assertThat(ev.email()).isEqualTo("ana@ex.com");
        assertThat(ev.produtoId()).isEqualTo("777");
        assertThat(ev.dataNascimento()).isEqualTo(LocalDate.of(1990, 3, 15));
    }

    @Test
    void jsonReembolso() {
        var ev = EduzzEvento.deJson(Map.of(
                "event", "myeduzz.invoice_refunded",
                "data", Map.of("id", "999", "buyer", Map.of("name", "Bia", "email", "bia@ex.com"),
                        "items", java.util.List.of(Map.of("productId", "42")))));
        assertThat(ev.cancelada()).isTrue();
        assertThat(ev.aprovada()).isFalse();
        assertThat(ev.transacao()).isEqualTo("999");
        assertThat(ev.produtoId()).isEqualTo("42");
        assertThat(ev.dataNascimento()).isNull();
    }

    @Test
    void senhaProvisoriaEhADataDeNascimento() {
        assertThat(SenhaProvisoria.para(LocalDate.of(1990, 3, 15))).isEqualTo("15031990");
        assertThat(SenhaProvisoria.para(null)).hasSize(8);
    }
}
