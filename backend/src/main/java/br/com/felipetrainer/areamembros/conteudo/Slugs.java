package br.com.felipetrainer.areamembros.conteudo;

import java.text.Normalizer;
import java.util.function.Predicate;

/** "Joelho Forte" -> "joelho-forte" (usado nos endereços das páginas). */
public final class Slugs {

    private Slugs() {}

    public static String unico(String titulo, Predicate<String> existe) {
        String base = Normalizer.normalize(titulo, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isEmpty()) base = "item";
        String slug = base;
        for (int i = 2; existe.test(slug); i++) slug = base + "-" + i;
        return slug;
    }
}
