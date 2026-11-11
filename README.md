# Área de membros: Felipe Trainer

Site próprio onde o aluno entra com login e senha e assiste aos vídeos dos exercícios dos cursos liberados para ele.
A venda acontece na Eduzz; o site só recebe o aviso da compra e libera o acesso.

## Como funciona

- **Vários cursos.** Cada curso tem capa, descrição, módulos (os cards, um por tipo de exercício), subtópicos e vídeos.
- **Liberação por usuário.** O aluno só assiste aos cursos liberados para ele. Os outros aparecem bloqueados, com o link da página de venda.
- **Compra na Eduzz.** A Eduzz avisa o site (webhook). O site cria o aluno (se for novo), libera o curso ligado ao código do produto e manda o e-mail com o link, o login e a senha provisória (data de nascimento, `ddmmaaaa`; sem a data, gera uma senha aleatória). Reembolso ou cancelamento bloqueia só aquele curso.
- **Primeiro acesso:** o aluno é obrigado a criar uma senha nova.
- **Imagens e vídeos no Cloudflare R2 ou no próprio servidor.** O admin envia pelo painel (com barra de progresso), troca e exclui. Com as variáveis `R2_*` preenchidas, os arquivos vão para o R2; sem elas, ficam na pasta `UPLOADS_DIR`. Ao excluir, o arquivo também é apagado. Os vídeos só abrem com um link assinado que vale 6 horas e só é entregue a quem tem o curso liberado. Também dá para usar link do YouTube ou Vimeo no lugar do arquivo.
- **Painel (`/admin`), só para admins:**
  - *Cursos e conteúdo:* cria e edita cursos, módulos, tópicos e vídeos, e envia as imagens e os vídeos.
  - *Usuários e acessos:* cria alunos e admins, escolhe quais cursos cada aluno pode ver, bloqueia, reenvia o e-mail de acesso e exclui. Admin vê todos os cursos. Ninguém consegue tirar o próprio acesso de admin, e sempre sobra pelo menos um admin ativo.
  - *Site:* nome, frase da tela de login e cor de destaque (o texto por cima da cor se ajusta sozinho).

## Tecnologias

| Parte | Tecnologia |
|---|---|
| Backend | Java 21, Spring Boot 4.1 (Web MVC, Data JPA, Security com JWT, Validation, Mail, upload multipart), Flyway |
| Banco | H2 em arquivo para desenvolvimento, PostgreSQL em produção |
| Frontend | React 19, TypeScript, Vite 8, Tailwind CSS 4, React Router |
| Deploy | Dockerfile único (o Spring serve o React) e docker-compose com PostgreSQL |

## Rodar no computador (desenvolvimento)

Precisa de **Java 21** e **Node 22+**. Não precisa instalar Maven (o projeto traz o `mvnw`).

Terminal 1, backend (porta 8080):

```bash
cd backend
./mvnw spring-boot:run        # no Windows: mvnw.cmd spring-boot:run
```

Terminal 2, frontend (porta 5173):

```bash
cd frontend
npm install
npm run dev
```

Abra http://localhost:5173 e entre com o admin padrão:

- e-mail: `admin@felipetrainer.com.br`
- senha: `admin123`

Na primeira vez, o sistema cria dois cursos de exemplo: "Fortalecimento para Atletas" (Joelho forte, Glúteo forte, Tornozelo forte, Treino de panturrilha) e "Mobilidade para Corredores", com vídeos "em breve". Para não criar, use `DADOS_EXEMPLO=false`. As imagens e vídeos enviados ficam em `backend/uploads`.

Sem SMTP configurado, os e-mails não são enviados: o texto aparece no log do backend (bom para testar).

### Simular uma compra da Eduzz

```bash
curl -d "trans_cod=123&trans_status=3&product_cod=777&cus_name=Maria&cus_email=maria@teste.com&cus_birth_date=15/03/1990" \
  "http://localhost:8080/api/webhooks/eduzz?token=troque-este-token"
```

Antes, coloque `777` no campo "Código do produto na Eduzz" do curso, no painel. Depois entre com `maria@teste.com` e a senha `15031990`. (Se só existir um curso, ele é liberado mesmo sem o código.)

## Produção

**Para colocar no ar de graça (Vercel + Render + Neon + Cloudflare R2), siga o [DEPLOY.md](DEPLOY.md).**

Com Docker, num servidor próprio:

```bash
docker compose up --build
```

Antes, troque no `docker-compose.yml` todas as senhas e segredos. Variáveis principais:

| Variável | Para que serve |
|---|---|
| `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD` | Banco PostgreSQL |
| `APP_URL` | Endereço público do site (vai no e-mail) |
| `JWT_SECRET` | Segredo do login, pelo menos 32 caracteres |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Admin criado na primeira subida |
| `EDUZZ_WEBHOOK_TOKEN` | Token que precisa vir na URL do webhook |
| `UPLOADS_DIR` | Pasta das imagens e vídeos enviados (faça backup dela junto com o banco) |
| `MAX_UPLOAD`, `MAX_VIDEO_MB` | Tamanho máximo de um vídeo (padrão 2 GB) |
| `R2_ACCOUNT_ID`, `R2_BUCKET`, `R2_ACCESS_KEY_ID`, `R2_SECRET_ACCESS_KEY` | Guardar imagens e vídeos no Cloudflare R2 |
| `CORS_ORIGINS` | Endereço do site quando ele fica em outro domínio (ex.: Vercel) |
| `VITE_API_URL` | (no build do frontend) endereço do backend, quando ele fica em outro domínio |
| `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD`, `EMAIL_FROM` | Envio de e-mail |

Na Eduzz, cadastre o webhook apontando para:

```
https://SEU-DOMINIO/api/webhooks/eduzz?token=VALOR_DE_EDUZZ_WEBHOOK_TOKEN
```

## Pendências antes de ir ao ar

1. **Conferir o formato do aviso da Eduzz.** O webhook aceita o formato antigo (formulário: `trans_cod`, `trans_status`, `cus_email`...) e JSON, mas os nomes dos campos e os códigos de status precisam ser conferidos com um aviso real da conta do Felipe. O log mostra o que chegou; os ajustes ficam em `backend/.../eduzz/EduzzEvento.java`.
2. **Data de nascimento no checkout.** Verificar se a Eduzz manda esse campo. Se não mandar, a senha provisória é aleatória (vai no e-mail do mesmo jeito).
3. **Espaço dos vídeos.** No R2 grátis cabem 10 GB; acima disso a Cloudflare cobra US$ 0,015 por GB/mês, sem custo de tráfego.
4. **Proxy na frente (Nginx etc.):** libere envios grandes (ex.: `client_max_body_size 2g;`).
5. **SMTP** para os e-mails (ex.: Amazon SES, Brevo, Resend, Gmail Workspace).

## Estrutura

```
backend/
  src/main/java/br/com/felipetrainer/areamembros/
    aluno/      usuários, matrículas (cursos liberados), compras, senha provisória
    auth/       login, troca de senha, token JWT
    conteudo/   site, cursos, módulos (cards), tópicos, vídeos; telas do aluno e do painel
    midia/      upload, armazenamento em disco e entrega das imagens e vídeos
    eduzz/      webhook da Eduzz
    email/      e-mail de boas-vindas
    config/     segurança, configurações, dados iniciais
    web/        erros e rotas do site
  src/main/resources/db/migration/   tabelas do banco (Flyway)
frontend/
  src/pages/        Login, TrocarSenha, MeusCursos, Curso, Aula
  src/pages/admin/  Painel: Cursos, Usuarios, SiteConfig
  src/components/   Navbar, VideoPlayer, Icones
docs/               transcrição do áudio do Felipe e requisitos
```
