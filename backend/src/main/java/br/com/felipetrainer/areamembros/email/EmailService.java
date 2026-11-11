package br.com.felipetrainer.areamembros.email;

import br.com.felipetrainer.areamembros.aluno.Usuario;
import br.com.felipetrainer.areamembros.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envia o e-mail de acesso. Sem SMTP configurado (SMTP_HOST vazio), só escreve o
 * e-mail no log, o que é suficiente para testar no computador.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final AppProperties props;
    private final String smtpHost;

    public EmailService(ObjectProvider<JavaMailSender> mailSender, AppProperties props,
                        @Value("${spring.mail.host:}") String smtpHost) {
        this.mailSender = mailSender;
        this.props = props;
        this.smtpHost = smtpHost;
    }

    public void enviarBoasVindas(Usuario aluno, String senhaProvisoria) {
        String dicaSenha = aluno.getDataNascimento() != null
                ? "(é a sua data de nascimento, só os números: ddmmaaaa)"
                : "";
        String texto = """
                Olá, %s!

                Seu acesso à área de membros do Felipe Trainer está liberado.

                Acesse: %s/login
                Login: %s
                Senha provisória: %s %s

                No primeiro acesso você vai criar uma senha nova.

                Bons treinos!
                Felipe Trainer
                """.formatted(aluno.getNome(), props.urlPublica(), aluno.getEmail(), senhaProvisoria, dicaSenha);

        enviar(aluno.getEmail(), "Seu acesso à área de membros", texto);
    }

    public void enviarCursoLiberado(Usuario aluno, String curso) {
        String texto = """
                Olá, %s!

                O curso "%s" foi liberado na sua conta.

                Acesse com o seu login de sempre: %s/login

                Bons treinos!
                Felipe Trainer
                """.formatted(aluno.getNome(), curso, props.urlPublica());
        enviar(aluno.getEmail(), "Novo curso liberado: " + curso, texto);
    }

    private void enviar(String para, String assunto, String texto) {
        if (smtpHost == null || smtpHost.isBlank()) {
            log.info("[E-MAIL NÃO ENVIADO: SMTP não configurado] Para: {} | {}\n{}", para, assunto, texto);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(props.emailRemetente());
            msg.setTo(para);
            msg.setSubject(assunto);
            msg.setText(texto);
            mailSender.getObject().send(msg);
        } catch (Exception e) {
            // Não derruba a liberação do acesso; o admin pode reenviar pelo painel.
            log.error("Falha ao enviar e-mail para {}: {}", para, e.getMessage());
        }
    }
}
