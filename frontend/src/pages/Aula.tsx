import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router";
import { api, type Modulo, type Video } from "../api";
import Navbar from "../components/Navbar";
import VideoPlayer from "../components/VideoPlayer";
import { Play, Seta } from "../components/Icones";

/** Página do módulo: player à esquerda e a lista de tópicos/vídeos ao lado. */
export default function Aula() {
  const { curso, modulo } = useParams();
  const [m, setM] = useState<Modulo | null>(null);
  const [erro, setErro] = useState("");
  const [atualId, setAtualId] = useState<number | null>(null);

  useEffect(() => {
    setM(null);
    setErro("");
    window.scrollTo(0, 0);
    api.get<Modulo>(`/cursos/${curso}/modulos/${modulo}`)
      .then((d) => { setM(d); setAtualId(d.topicos.flatMap((t) => t.videos)[0]?.id ?? null); })
      .catch((e) => setErro(e.message));
  }, [curso, modulo]);

  const lista = useMemo(() => m?.topicos.flatMap((t) => t.videos.map((v) => ({ v, t }))) ?? [], [m]);
  const pos = lista.findIndex((x) => x.v.id === atualId);
  const atual = lista[pos];
  const escolher = (v: Video) => { setAtualId(v.id); window.scrollTo({ top: 0, behavior: "smooth" }); };

  return (
    <div className="min-h-dvh pb-24">
      <Navbar />
      {erro && (
        <div className="mx-auto max-w-7xl px-4 pt-10 sm:px-8">
          <p className="text-red-300">{erro}</p>
          <Link to={`/curso/${curso}`} className="botao-sec mt-6">Voltar para o curso</Link>
        </div>
      )}
      {m && (
        <div className="mx-auto max-w-7xl px-4 pt-6 sm:px-8">
          <p className="text-sm text-suave">
            <Link to="/" className="hover:text-white">Meus cursos</Link> / <Link to={`/curso/${m.cursoSlug}`} className="hover:text-white">{m.cursoTitulo}</Link> /
          </p>
          <h1 className="titulo-esporte mt-2 text-5xl sm:text-6xl">{m.titulo}</h1>
          {m.descricao && <p className="mt-2 max-w-3xl text-suave">{m.descricao}</p>}

          <div className="mt-8 grid gap-8 lg:grid-cols-[1fr_360px]">
            <section>
              <VideoPlayer src={atual?.v.src ?? null} titulo={atual?.v.titulo ?? ""} />
              {atual && (
                <div className="mt-5">
                  <p className="text-xs font-semibold uppercase tracking-wider text-destaque">{atual.t.titulo}</p>
                  <h2 className="mt-1 text-2xl font-bold">{atual.v.titulo}</h2>
                  {atual.v.descricao && <p className="mt-2 leading-relaxed text-white/75">{atual.v.descricao}</p>}
                  {atual.t.descricao && <p className="mt-4 rounded-lg border border-linha bg-painel p-4 text-sm leading-relaxed text-suave">{atual.t.descricao}</p>}
                  <div className="mt-6 flex flex-wrap gap-3">
                    {pos > 0 && <button className="botao-sec" onClick={() => escolher(lista[pos - 1].v)}>Vídeo anterior</button>}
                    {pos < lista.length - 1 && <button className="botao" onClick={() => escolher(lista[pos + 1].v)}>Próximo vídeo <Seta /></button>}
                    {pos === lista.length - 1 && m.proximo && (
                      <Link to={`/curso/${m.cursoSlug}/${m.proximo.slug}`} className="botao">Próximo módulo: {m.proximo.titulo} <Seta /></Link>
                    )}
                  </div>
                </div>
              )}
              {lista.length === 0 && <p className="mt-6 text-suave">Os vídeos deste módulo vão aparecer aqui em breve.</p>}
            </section>

            <aside className="lg:sticky lg:top-20 lg:self-start">
              <div className="overflow-hidden rounded-xl border border-linha bg-painel">
                <p className="border-b border-linha px-4 py-3 text-sm font-semibold">Conteúdo do módulo</p>
                <div className="max-h-[70vh] overflow-y-auto">
                  {m.topicos.map((t, i) => (
                    <div key={t.id} className="border-b border-linha last:border-0">
                      <p className="flex items-baseline gap-2 px-4 pb-1 pt-4 text-xs font-bold uppercase tracking-wider text-suave">
                        <span className="text-destaque">{String(i + 1).padStart(2, "0")}</span>{t.titulo}
                      </p>
                      <ul className="pb-2">
                        {t.videos.map((v) => {
                          const ativo = v.id === atualId;
                          return (
                            <li key={v.id}>
                              <button onClick={() => escolher(v)} className={`flex w-full items-center gap-3 px-4 py-2.5 text-left text-sm transition ${ativo ? "bg-destaque/10 text-white" : "text-white/75 hover:bg-white/5"}`}>
                                <span className={`grid size-7 shrink-0 place-items-center rounded-full ${ativo ? "bg-destaque text-sobre-destaque" : "bg-painel-2 text-suave"}`}><Play className="ml-0.5 size-3" /></span>
                                <span className="min-w-0 flex-1 truncate">{v.titulo}</span>
                                {!v.src && <span className="text-[10px] uppercase text-suave">em breve</span>}
                              </button>
                            </li>
                          );
                        })}
                      </ul>
                    </div>
                  ))}
                </div>
              </div>
              <div className="mt-4 flex justify-between gap-3 text-sm">
                {m.anterior ? <Link to={`/curso/${m.cursoSlug}/${m.anterior.slug}`} className="text-suave hover:text-white">‹ {m.anterior.titulo}</Link> : <span />}
                {m.proximo && <Link to={`/curso/${m.cursoSlug}/${m.proximo.slug}`} className="text-suave hover:text-white">{m.proximo.titulo} ›</Link>}
              </div>
            </aside>
          </div>
        </div>
      )}
    </div>
  );
}
