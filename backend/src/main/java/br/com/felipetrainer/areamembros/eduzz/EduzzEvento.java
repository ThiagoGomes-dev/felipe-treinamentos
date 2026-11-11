package br.com.felipetrainer.areamembros.eduzz;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Os dados que importam de um aviso da Eduzz, já normalizados.
 *
 * Os nomes de campos abaixo cobrem o formato antigo (postback em formulário) e o formato
 * JSON mais novo, mas precisam ser conferidos com um aviso real antes de ir para produção.
 */
public record EduzzEvento(String transacao, String status, boolean aprovada, boolean cancelada,
                          String produtoId, String nome, String email, LocalDate dataNascimento) {

    // Status numéricos do postback antigo (trans_status): 3 = paga; 4 = cancelada; 6/7 = reembolso.
    private static final List<String> STATUS_APROVADO = List.of("3", "paid", "paga", "pago", "aprovada", "approved", "invoice_paid");
    private static final List<String> STATUS_CANCELADO = List.of("4", "6", "7", "canceled", "cancelled", "cancelada",
            "refunded", "reembolsada", "reembolsado", "chargeback", "invoice_refunded", "invoice_canceled");

    public static EduzzEvento deFormulario(Map<String, String> c) {
        return montar(c);
    }

    @SuppressWarnings("unchecked")
    public static EduzzEvento deJson(Map<String, Object> corpo) {
        Map<String, String> plano = new HashMap<>();
        achatar("", corpo, plano);
        return montar(plano);
    }

    private static EduzzEvento montar(Map<String, String> c) {
        String transacao = primeiro(c, "trans_cod", "data.id", "data.invoice.id", "invoice_id", "id");
        String status = primeiro(c, "trans_status", "event", "data.status", "status");
        String produto = primeiro(c, "product_cod", "prod_cod", "data.product.id", "data.productId",
                "data.items.0.productId", "data.items.0.product.id", "data.items.0.id", "product_id");
        String nome = primeiro(c, "cus_name", "data.buyer.name", "data.customer.name", "buyer.name", "name");
        String email = primeiro(c, "cus_email", "data.buyer.email", "data.customer.email", "buyer.email", "email");
        String nascimento = primeiro(c, "cus_birth_date", "cus_birthdate", "data.buyer.birthDate",
                "data.buyer.birth_date", "data.customer.birthDate", "birth_date");

        String s = status == null ? "" : status.toLowerCase(Locale.ROOT);
        boolean aprovada = STATUS_APROVADO.stream().anyMatch(x -> s.equals(x) || s.endsWith("." + x));
        boolean cancelada = STATUS_CANCELADO.stream().anyMatch(x -> s.equals(x) || s.endsWith("." + x));
        return new EduzzEvento(transacao, status, aprovada, cancelada, produto, nome, email, data(nascimento));
    }

    @SuppressWarnings("unchecked")
    private static void achatar(String prefixo, Map<String, Object> mapa, Map<String, String> saida) {
        mapa.forEach((k, v) -> valor(prefixo.isEmpty() ? k : prefixo + "." + k, v, saida));
    }

    @SuppressWarnings("unchecked")
    private static void valor(String chave, Object v, Map<String, String> saida) {
        if (v instanceof Map<?, ?> m) achatar(chave, (Map<String, Object>) m, saida);
        else if (v instanceof List<?> l) for (int i = 0; i < l.size(); i++) valor(chave + "." + i, l.get(i), saida);
        else if (v != null) saida.put(chave, String.valueOf(v));
    }

    private static String primeiro(Map<String, String> c, String... chaves) {
        for (String k : chaves) {
            String v = c.get(k);
            if (v != null && !v.isBlank()) return v.trim();
        }
        return null;
    }

    /** Aceita 1990-03-15, 15/03/1990 ou 1990-03-15T00:00:00. */
    static LocalDate data(String valor) {
        if (valor == null || valor.isBlank()) return null;
        String v = valor.trim();
        try {
            if (v.matches("\\d{2}/\\d{2}/\\d{4}")) return LocalDate.parse(v, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            return LocalDate.parse(v.length() > 10 ? v.substring(0, 10) : v);
        } catch (Exception e) {
            return null;
        }
    }
}
