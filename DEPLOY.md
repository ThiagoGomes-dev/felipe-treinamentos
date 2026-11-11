# Colocar no ar de graça (para teste)

| Parte | Onde | Custo |
|---|---|---|
| Site (React) | **Vercel** | grátis |
| Backend (Spring Boot) | **Render**, plano Free | grátis |
| Banco (PostgreSQL) | **Neon**, plano Free | grátis |
| Imagens (capas) | dentro do banco Neon | grátis |
| Vídeos | **YouTube** (não listado) ou Vimeo, colando o link no painel | grátis |

O código está em https://github.com/ThiagoGomes-dev/felipe-treinamentos

Limitações do plano grátis do Render:

- O backend "dorme" depois de 15 minutos sem uso, e o primeiro acesso depois disso demora uns 50 segundos.
- O disco é apagado a cada reinício. Por isso, nesta versão as imagens ficam no banco e o envio de arquivo de vídeo fica desligado: no painel, cole o link do YouTube (não listado) ou do Vimeo no vídeo. Quem tiver o link consegue abrir o vídeo fora do site.

Para produção de verdade, um servidor com disco fixo (Render pago com disco, ou uma VPS) libera o envio de vídeos pelo painel: é só trocar `UPLOAD_VIDEO` para `true` e definir `UPLOADS_DIR` numa pasta que não some.

Faça na ordem abaixo. Guarde num bloco de notas os valores marcados com 📝.

---

## 1. Banco no Neon

1. Entre em https://neon.tech e crie a conta (dá para entrar com o GitHub).
2. Crie um projeto: nome `felipe-area-membros`, Postgres 17, região **AWS US East (N. Virginia)** (fica perto do Render).
3. Na tela do projeto, clique em **Connect**. Desligue a opção "Connection pooling" e copie os dados:
   - 📝 **Host**, algo como `ep-xxxx-123456.us-east-1.aws.neon.tech`
   - 📝 **Database**, normalmente `neondb`
   - 📝 **Role** (usuário), normalmente `neondb_owner`
   - 📝 **Password**
4. Monte a URL do banco assim, trocando HOST e DATABASE:
   📝 `jdbc:postgresql://HOST/DATABASE?sslmode=require`

## 2. Backend no Render

1. Entre em https://render.com com a conta do GitHub e autorize o acesso ao repositório `felipe-treinamentos`.
2. Clique em **New** → **Blueprint**, escolha o repositório e confirme. O Render lê o arquivo `render.yaml` e cria o serviço `felipe-area-membros-api` no plano Free.
3. Ele vai pedir os valores das variáveis. Preencha:

| Variável | Valor |
|---|---|
| `DATABASE_URL` | a URL `jdbc:postgresql://...` do passo 1 |
| `DATABASE_USER` | o Role do Neon |
| `DATABASE_PASSWORD` | a senha do Neon |
| `ADMIN_EMAIL` | o seu e-mail de admin |
| `ADMIN_PASSWORD` | uma senha forte para o seu admin |
| `CORS_ORIGINS` | por enquanto `https://*.vercel.app` (no passo 4 você troca pelo endereço certo) |
| `APP_URL` | por enquanto `https://felipe.vercel.app` (troca no passo 4) |

   `JWT_SECRET` e `EDUZZ_WEBHOOK_TOKEN` são gerados sozinhos. `IMAGENS_NO_BANCO` e `UPLOAD_VIDEO` já vêm preenchidos.
4. O primeiro build leva de 5 a 10 minutos. Quando terminar, copie o endereço do serviço:
   📝 `https://felipe-area-membros-api.onrender.com` (o seu pode ter um final diferente).
5. Teste abrindo `https://SEU-BACKEND.onrender.com/api/site`. Tem que aparecer um texto com o nome do site.

## 3. Site na Vercel

1. Entre em https://vercel.com com a conta do GitHub.
2. **Add New** → **Project**, importe `felipe-treinamentos`.
3. Em **Root Directory**, clique em Edit e escolha a pasta **`frontend`**. O framework aparece como Vite.
4. Em **Environment Variables**, adicione:
   - `VITE_API_URL` = o endereço do backend do passo 2, sem barra no final
5. Clique em **Deploy**. No fim, copie o 📝 endereço do site, ex.: `https://felipe-treinamentos.vercel.app`.

## 4. Acertar os endereços

No Render, em **Environment**, troque:

- `CORS_ORIGINS` = o endereço da Vercel (ex.: `https://felipe-treinamentos.vercel.app`)
- `APP_URL` = o mesmo endereço (vai no e-mail de boas-vindas)

Salve. O Render reinicia sozinho.

## 5. Criar o login do cliente

1. Abra o site da Vercel e entre com `ADMIN_EMAIL` e `ADMIN_PASSWORD`.
2. Vá em **Painel → Usuários e acessos** e clique em **Novo usuário**:
   - Para o Felipe cuidar do conteúdo: papel **Admin**.
   - Para ele ver como aluno: papel **Aluno** e marque os cursos.
3. **Preencha a data de nascimento.** A senha provisória é essa data no formato `ddmmaaaa` (ex.: 15/03/1990 vira `15031990`). Sem SMTP o e-mail não sai, então mande o login e essa senha para ele. No primeiro acesso, o site pede para criar uma senha nova.

Os dois cursos de exemplo aparecem na primeira subida. Pode editar ou apagar pelo painel.

---

## Depois (quando for para valer)

- **E-mail:** sem SMTP, os e-mails não saem (aparecem só no log do Render). Para enviar de verdade, crie uma conta grátis no Brevo ou Resend e preencha `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD` e `EMAIL_FROM` no Render.
- **Eduzz:** cadastre o webhook `https://SEU-BACKEND.onrender.com/api/webhooks/eduzz?token=VALOR` (o valor está em `EDUZZ_WEBHOOK_TOKEN`, no Render).
- **Domínio próprio:** dá para ligar um domínio (ex.: `membros.felipetrainer.com.br`) na Vercel. Depois atualize `CORS_ORIGINS` e `APP_URL`.

## Atualizar

Todo `git push` na branch `main` faz o Render e a Vercel publicarem a versão nova sozinhos.
