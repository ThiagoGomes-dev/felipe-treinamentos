// Cliente da API do Spring Boot. O token de login fica no localStorage.

export type Papel = "ALUNO" | "ADMIN";

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  papel: Papel;
  senhaProvisoria: boolean;
}

export interface Site {
  nome: string;
  slogan: string | null;
  corDestaque: string;
}

export interface CursoCard {
  id: number;
  slug: string;
  titulo: string;
  subtitulo: string | null;
  descricao: string | null;
  capaUrl: string | null;
  linkVenda: string | null;
  liberado: boolean;
  totalModulos: number;
  totalVideos: number;
}

export interface ModuloCard {
  id: number;
  slug: string;
  titulo: string;
  descricao: string | null;
  capaUrl: string | null;
  totalTopicos: number;
  totalVideos: number;
}

export interface Curso extends Omit<CursoCard, "totalModulos" | "totalVideos"> {
  modulos: ModuloCard[];
}

export interface Video {
  id: number;
  titulo: string;
  descricao: string | null;
  ordem: number;
  src: string | null;
  url: string | null;
  arquivoId: string | null;
  arquivoNome: string | null;
  arquivoTamanho: number | null;
}

export interface Topico {
  id: number;
  titulo: string;
  descricao: string | null;
  ordem: number;
  videos: Video[];
}

export interface Modulo {
  id: number;
  slug: string;
  titulo: string;
  descricao: string | null;
  capaUrl: string | null;
  cursoSlug: string;
  cursoTitulo: string;
  anterior: { slug: string; titulo: string } | null;
  proximo: { slug: string; titulo: string } | null;
  topicos: Topico[];
}

export interface AdminModulo {
  id: number;
  slug: string;
  titulo: string;
  descricao: string | null;
  capaId: string | null;
  capaUrl: string | null;
  ordem: number;
  publicado: boolean;
  topicos: Topico[];
}

export interface AdminCurso {
  id: number;
  slug: string;
  titulo: string;
  subtitulo: string | null;
  descricao: string | null;
  capaId: string | null;
  capaUrl: string | null;
  eduzzProdutoId: string | null;
  linkVenda: string | null;
  ordem: number;
  publicado: boolean;
  alunos: number;
  modulos: AdminModulo[];
}

export interface UsuarioAdmin {
  id: number;
  nome: string;
  email: string;
  dataNascimento: string | null;
  papel: Papel;
  ativo: boolean;
  senhaProvisoria: boolean;
  origem: "MANUAL" | "EDUZZ";
  criadoEm: string;
  cursos: number[];
}

/**
 * Endereço do backend. Vazio = mesmo domínio (desenvolvimento ou tudo num servidor só).
 * Na Vercel, defina VITE_API_URL=https://seu-backend.onrender.com
 */
export const BASE_API = (import.meta.env.VITE_API_URL ?? "").replace(/\/$/, "");

/** Imagens e vídeos do servidor vêm como "/api/midia/..."; aponta para o backend certo. */
export function midia(url: string | null | undefined): string | null {
  if (!url) return null;
  return url.startsWith("/api/") ? BASE_API + url : url;
}

const CHAVE_TOKEN = "am.token";

export const token = {
  get: () => {
    try { return localStorage.getItem(CHAVE_TOKEN); } catch { return null; }
  },
  set: (t: string | null) => {
    try { t ? localStorage.setItem(CHAVE_TOKEN, t) : localStorage.removeItem(CHAVE_TOKEN); } catch { /* sem storage */ }
  },
};

export class ErroApi extends Error {
  constructor(public status: number, mensagem: string) {
    super(mensagem);
  }
}

function mensagemDe(status: number, corpo: string) {
  try {
    const j = JSON.parse(corpo);
    if (j.detail || j.message) return j.detail || j.message;
  } catch { /* sem json */ }
  if (status === 413) return "Arquivo grande demais para o servidor.";
  return "Algo deu errado. Tente de novo.";
}

function aoNaoAutorizado(status: number, caminho: string) {
  if (status === 401 && caminho !== "/auth/login") {
    token.set(null);
    window.dispatchEvent(new Event("am:sair"));
  }
}

async function chamar<T>(metodo: string, caminho: string, corpo?: unknown): Promise<T> {
  const headers: Record<string, string> = {};
  const t = token.get();
  if (t) headers.Authorization = `Bearer ${t}`;
  if (corpo !== undefined) headers["Content-Type"] = "application/json";

  const resp = await fetch(`${BASE_API}/api${caminho}`, {
    method: metodo,
    headers,
    body: corpo === undefined ? undefined : JSON.stringify(corpo),
  });
  aoNaoAutorizado(resp.status, caminho);
  if (!resp.ok) throw new ErroApi(resp.status, mensagemDe(resp.status, await resp.text()));
  if (resp.status === 204) return undefined as T;
  return resp.json() as Promise<T>;
}

/** Envia um arquivo mostrando o progresso (0 a 100). */
function enviar<T>(caminho: string, arquivo: File, progresso?: (p: number) => void): Promise<T> {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    xhr.open("POST", `${BASE_API}/api${caminho}`);
    const t = token.get();
    if (t) xhr.setRequestHeader("Authorization", `Bearer ${t}`);
    xhr.upload.onprogress = (e) => e.lengthComputable && progresso?.(Math.round((e.loaded / e.total) * 100));
    xhr.onload = () => {
      aoNaoAutorizado(xhr.status, caminho);
      if (xhr.status >= 200 && xhr.status < 300) resolve(JSON.parse(xhr.responseText));
      else reject(new ErroApi(xhr.status, mensagemDe(xhr.status, xhr.responseText)));
    };
    xhr.onerror = () => reject(new ErroApi(0, "Falha de conexão durante o envio."));
    const form = new FormData();
    form.append("arquivo", arquivo);
    xhr.send(form);
  });
}

export const api = {
  get: <T,>(c: string) => chamar<T>("GET", c),
  post: <T,>(c: string, b?: unknown) => chamar<T>("POST", c, b ?? {}),
  put: <T,>(c: string, b: unknown) => chamar<T>("PUT", c, b),
  del: <T = void,>(c: string) => chamar<T>("DELETE", c),
  enviar,
};

/** Aplica a cor de destaque no site todo e escolhe texto claro ou escuro por cima dela. */
export function aplicarCor(cor?: string | null) {
  if (!cor || !/^#[0-9a-f]{6}$/i.test(cor)) return;
  const [r, g, b] = [1, 3, 5].map((i) => parseInt(cor.slice(i, i + 2), 16) / 255)
    .map((c) => (c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4));
  const lum = 0.2126 * r + 0.7152 * g + 0.0722 * b;
  const raiz = document.documentElement.style;
  raiz.setProperty("--destaque", cor);
  raiz.setProperty("--sobre-destaque", lum > 0.35 ? "#0e0f11" : "#ffffff");
}

export function tamanhoLegivel(bytes: number) {
  if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))} KB`;
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
  return `${(bytes / 1024 / 1024 / 1024).toFixed(2)} GB`;
}
