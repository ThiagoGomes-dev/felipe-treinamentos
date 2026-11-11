package br.com.felipetrainer.areamembros.config;

import br.com.felipetrainer.areamembros.aluno.Papel;
import br.com.felipetrainer.areamembros.aluno.Usuario;
import br.com.felipetrainer.areamembros.aluno.UsuarioRepository;
import br.com.felipetrainer.areamembros.conteudo.Curso;
import br.com.felipetrainer.areamembros.conteudo.CursoRepository;
import br.com.felipetrainer.areamembros.conteudo.Modulo;
import br.com.felipetrainer.areamembros.conteudo.Site;
import br.com.felipetrainer.areamembros.conteudo.SiteRepository;
import br.com.felipetrainer.areamembros.conteudo.Topico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Cria o admin, as configurações do site e (opcionalmente) cursos de exemplo na primeira vez que o sistema sobe. */
@Component
public class DadosIniciais implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);

    private final UsuarioRepository usuarios;
    private final SiteRepository sites;
    private final CursoRepository cursos;
    private final PasswordEncoder encoder;
    private final AppProperties props;

    public DadosIniciais(UsuarioRepository usuarios, SiteRepository sites, CursoRepository cursos,
                         PasswordEncoder encoder, AppProperties props) {
        this.usuarios = usuarios;
        this.sites = sites;
        this.cursos = cursos;
        this.encoder = encoder;
        this.props = props;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!usuarios.existsByPapel(Papel.ADMIN)) {
            Usuario admin = new Usuario();
            admin.setNome("Felipe");
            admin.setEmail(props.admin().email().toLowerCase());
            admin.setSenhaHash(encoder.encode(props.admin().senha()));
            admin.setPapel(Papel.ADMIN);
            admin.setSenhaProvisoria(false);
            usuarios.save(admin);
            log.info("Admin criado: {}", admin.getEmail());
        }

        if (sites.count() == 0) {
            Site s = new Site();
            s.setNome("Felipe Trainer");
            s.setSlogan("Treino de força para quem leva o esporte a sério.");
            sites.save(s);
        }

        if (props.dadosExemplo() && cursos.count() == 0) {
            criarExemplos();
            log.info("Cursos de exemplo criados");
        }
    }

    private void criarExemplos() {
        Curso c = curso("fortalecimento-para-atletas", "Fortalecimento para Atletas",
                "Mais força, menos lesão, mais desempenho.",
                "Programa de fortalecimento com vídeos de cada exercício, organizado por região do corpo. "
                        + "Escolha um módulo, assista e treine.", 0);
        modulo(c, "joelho-forte", "Joelho Forte", "Estabilidade e força para proteger o joelho na corrida e nos saltos.",
                new String[][] {
                        {"Ativação", "Exercícios leves para preparar a musculatura antes do treino."},
                        {"Fortalecimento", "Exercícios principais. Faça 3 séries de 12 repetições, com controle."},
                        {"Estabilidade", "Equilíbrio e controle do joelho em apoio unilateral."}});
        modulo(c, "gluteo-forte", "Glúteo Forte", "O motor da passada: potência e proteção para quadril e lombar.",
                new String[][] {
                        {"Ativação", "Acorde o glúteo antes de treinar."},
                        {"Fortalecimento", "Ganho de força com elástico e carga."}});
        modulo(c, "tornozelo-forte", "Tornozelo Forte", "Mobilidade e estabilidade para evitar entorses.",
                new String[][] {
                        {"Mobilidade", "Amplitude de movimento do tornozelo."},
                        {"Propriocepção", "Equilíbrio e reação em superfícies instáveis."}});
        modulo(c, "treino-de-panturrilha", "Treino de Panturrilha", "Resistência e força para aguentar o volume de treino.",
                new String[][] {{"Fortalecimento", "Elevações de panturrilha em diferentes variações."}});
        cursos.save(c);

        Curso c2 = curso("mobilidade-para-corredores", "Mobilidade para Corredores",
                "Solte o corpo e corra com mais amplitude.",
                "Rotinas curtas de mobilidade para antes e depois da corrida.", 1);
        modulo(c2, "quadril", "Quadril", "Mobilidade de quadril para uma passada mais longa.",
                new String[][] {{"Rotina pré-treino", "10 minutos antes de correr."}});
        cursos.save(c2);
    }

    private Curso curso(String slug, String titulo, String subtitulo, String descricao, int ordem) {
        Curso c = new Curso();
        c.setSlug(slug);
        c.setTitulo(titulo);
        c.setSubtitulo(subtitulo);
        c.setDescricao(descricao);
        c.setOrdem(ordem);
        return c;
    }

    private void modulo(Curso c, String slug, String titulo, String descricao, String[][] topicos) {
        Modulo m = c.adicionarModulo(slug, titulo, descricao);
        for (String[] t : topicos) {
            Topico topico = m.adicionarTopico(t[0], t[1]);
            // Vídeos sem arquivo aparecem como "vídeo em breve" até o Felipe enviar pelo painel.
            topico.adicionarVideo("Exercício 1", null);
            topico.adicionarVideo("Exercício 2", null);
        }
    }
}
