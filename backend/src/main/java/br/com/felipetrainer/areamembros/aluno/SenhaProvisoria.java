package br.com.felipetrainer.areamembros.aluno;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Regra do Felipe: a senha provisória é a data de nascimento do aluno (ddMMaaaa, ex.: 15031990).
 * Se a data não vier na compra, gera uma senha aleatória de 8 caracteres.
 */
public final class SenhaProvisoria {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final String CARACTERES = "abcdefghjkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private SenhaProvisoria() {}

    public static String para(LocalDate dataNascimento) {
        if (dataNascimento != null) return dataNascimento.format(FORMATO);
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) sb.append(CARACTERES.charAt(RANDOM.nextInt(CARACTERES.length())));
        return sb.toString();
    }
}
