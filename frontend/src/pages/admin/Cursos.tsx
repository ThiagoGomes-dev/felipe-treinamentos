import { useCallback, useEffect, useState, type FormEvent } from "react";
import { api, type AdminCurso, type AdminModulo, type Topico, type Video } from "../../api";
import { Lixeira, Mais } from "../../components/Icones";
import { Caixa, Campo, ImagemUpload, VideoUpload, useAviso } from "./ui";

/** Ação que devolve o curso atualizado. */
type Acao = (fazer: () => Promise<AdminCurso | void>, ok?: string) => Promise<void>;
type Envio = (caminho: string) => (f: File, progresso: (p: number) => void) => Promise<void>;

export default function Cursos() {
  const [cursos, setCursos] = useState<AdminCurso[]>([]);
  const [selId, setSelId] = useState<number | null>(null);
  const { avisar, el } = useAviso();

  const carregar = useCallback(async () => {
    const cs = await api.get<AdminCurso[]>("/admin/cursos");
    setCursos(cs);
    setSelId((s) => (s && cs.some((c) => c.id === s) ? s : cs[0]?.id ?? null));
  }, []);

  useEffect(() => { carregar().catch((e) => avisar(e.message, true)); }, [carregar, avisar]);

  const acao: Acao = async (fazer, ok = "Salvo") => {
    try {
      const r = await fazer();
      if (r) setCursos((cs) => cs.map((c) => (c.id === r.id ? r : c)));
      else await carregar();
      avisar(ok);
    } catch (e) {
      avisar((e as Error).message, true);
    }
  };

  const envio: Envio = (caminho) => async (f, progresso) => {
    try {
      const r = await api.enviar<AdminCurso>(caminho, f, progresso);
      setCursos((cs) => cs.map((c) => (c.id === r.id ? r : c)));
      avisar("Arquivo enviado");
    } catch (e) {
      avisar((e as Error).message, true);
    }
  };

  const novoCurso = async () => {
    try {
      const c = await api.post<AdminCurso>("/admin/cursos", { titulo: "Novo curso", publicado: false });
      setCursos((cs) => [...cs, c]);
      setSelId(c.id);
      avisar("Curso criado");
    } catch (e) { avisar((e as Error).message, true); }
  };

  const sel = cursos.find((c) => c.id === selId);

  return (
    <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
      <div>
        <div className="flex items-center justify-between">
          <p className="text-xs font-bold uppercase tracking-[0.15em] text-suave">Cursos</p>
          <button className="botao-mini" onClick={novoCurso}><Mais className="size-3.5" />Novo</button>
        </div>
        <ul className="mt-3 space-y-1">
          {cursos.map((c) => (
            <li key={c.id}>
              <button onClick={() => setSelId(c.id)} className={`w-full rounded-lg px-3 py-2.5 text-left text-sm ${c.id === selId ? "bg-painel-2 text-white ring-1 ring-linha" : "text-white/70 hover:bg-white/5"}`}>
                <span className="block truncate font-medium">{c.titulo}</span>
                <span className="mt-0.5 flex items-center gap-2 text-[11px] text-suave">
                  {c.modulos.length} módulos · {c.alunos} alunos
                  {!c.publicado && <span className="etiqueta bg-yellow-500/15 px-1.5 text-[10px] text-yellow-300">Rascunho</span>}
                </span>
              </button>
            </li>
          ))}
        </ul>
      </div>
      {sel ? <EditorCurso key={sel.id} c={sel} acao={acao} envio={envio} /> : <p className="text-suave">Crie o primeiro curso.</p>}
      {el}
    </div>
  );
}

function EditorCurso({ c, acao, envio }: { c: AdminCurso; acao: Acao; envio: Envio }) {
  const [f, setF] = useState({
    titulo: c.titulo, subtitulo: c.subtitulo ?? "", descricao: c.descricao ?? "", eduzzProdutoId: c.eduzzProdutoId ?? "",
    linkVenda: c.linkVenda ?? "", ordem: c.ordem, publicado: c.publicado,
  });
  const [aberto, setAberto] = useState<number | null>(c.modulos[0]?.id ?? null);

  const salvar = (e: FormEvent) => { e.preventDefault(); acao(() => api.put<AdminCurso>(`/admin/cursos/${c.id}`, f), "Curso salvo"); };
  const excluir = () => {
    if (confirm(`Excluir o curso "${c.titulo}"? Todos os módulos, vídeos e imagens dele serão apagados do servidor.`)) {
      acao(() => api.del(`/admin/cursos/${c.id}`), "Curso excluído");
    }
  };
  const novoModulo = () => acao(async () => {
    const r = await api.post<AdminCurso>(`/admin/cursos/${c.id}/modulos`, { titulo: "Novo módulo", publicado: false });
    setAberto(r.modulos[r.modulos.length - 1]?.id ?? null);
    return r;
  }, "Módulo criado");

  return (
    <div className="space-y-6">
      <Caixa>
        <form onSubmit={salvar} className="grid gap-4 md:grid-cols-2">
          <Campo rotulo="Nome do curso"><input className="campo" required value={f.titulo} onChange={(e) => setF({ ...f, titulo: e.target.value })} /></Campo>
          <Campo rotulo="Frase de destaque"><input className="campo" value={f.subtitulo} onChange={(e) => setF({ ...f, subtitulo: e.target.value })} /></Campo>
          <Campo rotulo="Descrição" className="md:col-span-2"><textarea className="campo" rows={3} value={f.descricao} onChange={(e) => setF({ ...f, descricao: e.target.value })} /></Campo>
          <Campo rotulo="Código do produto na Eduzz" dica="A compra desse produto libera este curso automaticamente."><input className="campo" value={f.eduzzProdutoId} onChange={(e) => setF({ ...f, eduzzProdutoId: e.target.value })} placeholder="ex.: 1234567" /></Campo>
          <Campo rotulo="Link da página de venda" dica="Aparece para quem ainda não tem o curso."><input className="campo" value={f.linkVenda} onChange={(e) => setF({ ...f, linkVenda: e.target.value })} placeholder="https://..." /></Campo>
          <Campo rotulo="Ordem na lista"><input className="campo" type="number" value={f.ordem} onChange={(e) => setF({ ...f, ordem: Number(e.target.value) })} /></Campo>
          <label className="flex items-center gap-2 self-end pb-2.5 text-sm"><input type="checkbox" className="size-4 accent-[var(--destaque)]" checked={f.publicado} onChange={(e) => setF({ ...f, publicado: e.target.checked })} /> Publicado (aparece para os alunos)</label>
          <div className="md:col-span-2">
            <ImagemUpload rotulo="Capa do curso (horizontal)" formato="4/3" url={c.capaUrl}
              aoEnviar={envio(`/admin/cursos/${c.id}/capa`)}
              aoRemover={() => acao(() => api.del<AdminCurso>(`/admin/cursos/${c.id}/capa`), "Imagem excluída")} />
          </div>
          <div className="flex flex-wrap justify-end gap-2 md:col-span-2">
            <button type="button" className="botao-perigo" onClick={excluir}><Lixeira className="size-3.5" />Excluir curso</button>
            <button className="botao !py-2">Salvar curso</button>
          </div>
        </form>
      </Caixa>

      <div className="flex items-center justify-between">
        <p className="text-xs font-bold uppercase tracking-[0.15em] text-suave">Módulos (cards do curso)</p>
        <button className="botao-mini" onClick={novoModulo}><Mais className="size-3.5" />Novo módulo</button>
      </div>
      {c.modulos.length === 0 && <p className="text-sm text-suave">Nenhum módulo ainda.</p>}
      {c.modulos.map((m, i) => (
        <EditorModulo key={m.id} m={m} numero={i + 1} aberto={aberto === m.id} alternar={() => setAberto(aberto === m.id ? null : m.id)} acao={acao} envio={envio} />
      ))}
    </div>
  );
}

function EditorModulo({ m, numero, aberto, alternar, acao, envio }: { m: AdminModulo; numero: number; aberto: boolean; alternar: () => void; acao: Acao; envio: Envio }) {
  const [f, setF] = useState({ titulo: m.titulo, descricao: m.descricao ?? "", ordem: m.ordem, publicado: m.publicado });
  const videos = m.topicos.reduce((s, t) => s + t.videos.length, 0);
  const salvar = (e: FormEvent) => { e.preventDefault(); acao(() => api.put<AdminCurso>(`/admin/modulos/${m.id}`, f), "Módulo salvo"); };
  const excluir = () => confirm(`Excluir o módulo "${m.titulo}" e todos os vídeos dele?`) && acao(() => api.del<AdminCurso>(`/admin/modulos/${m.id}`), "Módulo excluído");
  const novoTopico = () => acao(() => api.post<AdminCurso>(`/admin/modulos/${m.id}/topicos`, { titulo: "Novo tópico" }), "Tópico criado");

  return (
    <div className="overflow-hidden rounded-xl border border-linha bg-painel">
      <button onClick={alternar} className="flex w-full items-center gap-4 px-5 py-4 text-left hover:bg-white/[0.03]">
        <span className="titulo-esporte text-3xl text-destaque">{String(numero).padStart(2, "0")}</span>
        <span className="min-w-0 flex-1">
          <span className="block truncate font-semibold">{m.titulo}</span>
          <span className="text-xs text-suave">{m.topicos.length} tópicos · {videos} vídeos</span>
        </span>
        {!m.publicado && <span className="etiqueta bg-yellow-500/15 text-yellow-300">Rascunho</span>}
        <span className={`text-suave transition ${aberto ? "rotate-90" : ""}`}>›</span>
      </button>
      {aberto && (
        <div className="space-y-5 border-t border-linha p-5">
          <form onSubmit={salvar} className="grid gap-4 md:grid-cols-[1fr_120px]">
            <Campo rotulo="Título do card"><input className="campo" required value={f.titulo} onChange={(e) => setF({ ...f, titulo: e.target.value })} /></Campo>
            <Campo rotulo="Ordem"><input className="campo" type="number" value={f.ordem} onChange={(e) => setF({ ...f, ordem: Number(e.target.value) })} /></Campo>
            <Campo rotulo="Descrição" className="md:col-span-2"><textarea className="campo" rows={2} value={f.descricao} onChange={(e) => setF({ ...f, descricao: e.target.value })} /></Campo>
            <div className="md:col-span-2">
              <ImagemUpload rotulo="Imagem do card (quadrada)" formato="1/1" url={m.capaUrl}
                aoEnviar={envio(`/admin/modulos/${m.id}/capa`)}
                aoRemover={() => acao(() => api.del<AdminCurso>(`/admin/modulos/${m.id}/capa`), "Imagem excluída")} />
            </div>
            <div className="flex flex-wrap items-center justify-between gap-3 md:col-span-2">
              <label className="flex items-center gap-2 text-sm"><input type="checkbox" className="size-4 accent-[var(--destaque)]" checked={f.publicado} onChange={(e) => setF({ ...f, publicado: e.target.checked })} /> Publicado</label>
              <div className="flex gap-2">
                <button type="button" className="botao-perigo" onClick={excluir}><Lixeira className="size-3.5" />Excluir módulo</button>
                <button className="botao !py-2">Salvar módulo</button>
              </div>
            </div>
          </form>

          <div className="flex items-center justify-between border-t border-linha pt-5">
            <p className="text-xs font-bold uppercase tracking-[0.15em] text-suave">Subtópicos</p>
            <button className="botao-mini" onClick={novoTopico}><Mais className="size-3.5" />Novo tópico</button>
          </div>
          {m.topicos.map((t, i) => <EditorTopico key={t.id} t={t} numero={i + 1} acao={acao} envio={envio} />)}
        </div>
      )}
    </div>
  );
}

function EditorTopico({ t, numero, acao, envio }: { t: Topico; numero: number; acao: Acao; envio: Envio }) {
  const [f, setF] = useState({ titulo: t.titulo, descricao: t.descricao ?? "", ordem: t.ordem });
  const salvar = (e: FormEvent) => { e.preventDefault(); acao(() => api.put<AdminCurso>(`/admin/topicos/${t.id}`, f), "Tópico salvo"); };
  const excluir = () => confirm(`Excluir o tópico "${t.titulo}" e os vídeos dele?`) && acao(() => api.del<AdminCurso>(`/admin/topicos/${t.id}`), "Tópico excluído");
  const novoVideo = () => acao(() => api.post<AdminCurso>(`/admin/topicos/${t.id}/videos`, { titulo: "Novo exercício" }), "Vídeo adicionado");

  return (
    <div className="rounded-lg border border-linha bg-fundo/60 p-4">
      <form onSubmit={salvar} className="grid gap-3 sm:grid-cols-[auto_1fr_90px]">
        <span className="titulo-esporte pt-1 text-2xl text-suave">{String(numero).padStart(2, "0")}</span>
        <input className="campo" required value={f.titulo} onChange={(e) => setF({ ...f, titulo: e.target.value })} aria-label="Título do tópico" />
        <input className="campo" type="number" value={f.ordem} onChange={(e) => setF({ ...f, ordem: Number(e.target.value) })} aria-label="Ordem" />
        <textarea className="campo sm:col-start-2 sm:col-end-4" rows={2} placeholder="Descrição leve do tópico" value={f.descricao} onChange={(e) => setF({ ...f, descricao: e.target.value })} />
        <div className="flex justify-end gap-2 sm:col-start-2 sm:col-end-4">
          <button type="button" className="botao-perigo" onClick={excluir}><Lixeira className="size-3.5" />Excluir tópico</button>
          <button className="botao-mini !border-destaque/60 !text-white">Salvar tópico</button>
        </div>
      </form>
      <div className="mt-4 space-y-3 border-t border-linha pt-4">
        <div className="flex items-center justify-between">
          <p className="text-xs font-semibold text-suave">Vídeos</p>
          <button className="botao-mini" onClick={novoVideo}><Mais className="size-3.5" />Vídeo</button>
        </div>
        {t.videos.map((v) => <EditorVideo key={v.id} v={v} acao={acao} envio={envio} />)}
      </div>
    </div>
  );
}

function EditorVideo({ v, acao, envio }: { v: Video; acao: Acao; envio: Envio }) {
  const [f, setF] = useState({ titulo: v.titulo, descricao: v.descricao ?? "", url: v.url ?? "", ordem: v.ordem });
  const [link, setLink] = useState(!!v.url && !v.arquivoId);
  const salvar = (e: FormEvent) => { e.preventDefault(); acao(() => api.put<AdminCurso>(`/admin/videos/${v.id}`, { ...f, url: link ? f.url : "" }), "Vídeo salvo"); };
  const excluir = () => confirm(`Excluir o vídeo "${v.titulo}"?`) && acao(() => api.del<AdminCurso>(`/admin/videos/${v.id}`), "Vídeo excluído");

  return (
    <form onSubmit={salvar} className="grid gap-2 rounded-lg border border-linha bg-painel p-3 md:grid-cols-2">
      <input className="campo" required value={f.titulo} onChange={(e) => setF({ ...f, titulo: e.target.value })} aria-label="Nome do exercício" placeholder="Nome do exercício" />
      <input className="campo" value={f.descricao} onChange={(e) => setF({ ...f, descricao: e.target.value })} aria-label="Descrição" placeholder="Dica rápida (opcional)" />
      <div className="md:col-span-2">
        {link ? (
          <input className="campo" placeholder="Link do YouTube, Vimeo ou .mp4" value={f.url} onChange={(e) => setF({ ...f, url: e.target.value })} aria-label="Link do vídeo" />
        ) : (
          <VideoUpload nome={v.arquivoNome} tamanho={v.arquivoTamanho}
            aoEnviar={envio(`/admin/videos/${v.id}/arquivo`)}
            aoRemover={() => acao(() => api.del<AdminCurso>(`/admin/videos/${v.id}/arquivo`), "Vídeo excluído do servidor")} />
        )}
      </div>
      <div className="flex flex-wrap items-center justify-between gap-2 md:col-span-2">
        <button type="button" className="text-[11px] text-suave underline-offset-2 hover:text-white hover:underline" onClick={() => setLink(!link)}>
          {link ? "Usar arquivo enviado ao servidor" : "Usar link externo (YouTube/Vimeo)"}
        </button>
        <div className="flex gap-2">
          <button type="button" className="botao-perigo" onClick={excluir} aria-label="Excluir vídeo"><Lixeira className="size-3.5" /></button>
          <button className="botao-mini">Salvar</button>
        </div>
      </div>
    </form>
  );
}
