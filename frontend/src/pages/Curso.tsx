import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import { api, midia, type Curso } from "../api";
import Navbar from "../components/Navbar";
import { Cadeado, Seta } from "../components/Icones";

export default function CursoPage() {
  const { curso: slug } = useParams();
  const [c, setC] = useState<Curso | null>(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    setC(null);
    api.get<Curso>(`/cursos/${slug}`).then(setC).catch((e) => setErro(e.message));
  }, [slug]);

  const total = c?.modulos.reduce((s, m) => s + m.totalVideos, 0) ?? 0;

  return (
    <div className="min-h-dvh pb-24">
      <Navbar />
      {erro && <p className="mx-auto max-w-7xl px-4 pt-10 text-sm text-red-300 sm:px-8">{erro}</p>}
      {c && (
        <>
          <header className="border-b border-linha">
            <div className="mx-auto grid max-w-7xl items-center gap-10 px-4 py-10 sm:px-8 lg:grid-cols-[1fr_0.9fr] lg:py-14">
              <div>
                <Link to="/" className="text-sm text-suave hover:text-white">Meus cursos /</Link>
                <h1 className="titulo-esporte mt-3 text-6xl sm:text-7xl">{c.titulo}</h1>
                {c.subtitulo && <p className="mt-4 text-xl font-medium text-white/85">{c.subtitulo}</p>}
                {c.descricao && <p className="mt-3 max-w-xl leading-relaxed text-suave">{c.descricao}</p>}
                <div className="mt-6 flex flex-wrap gap-2">
                  <span className="etiqueta bg-painel-2 text-white/80">{c.modulos.length} módulos</span>
                  <span className="etiqueta bg-painel-2 text-white/80">{total} vídeos</span>
                  {!c.liberado && <span className="etiqueta bg-red-500/15 text-red-300"><Cadeado className="size-3" /> Não liberado</span>}
                </div>
                <div className="mt-8">
                  {c.liberado && c.modulos[0] && (
                    <Link to={`/curso/${c.slug}/${c.modulos[0].slug}`} className="botao">Começar pelo módulo 1 <Seta /></Link>
                  )}
                  {!c.liberado && (c.linkVenda
                    ? <a href={c.linkVenda} target="_blank" rel="noreferrer" className="botao">Quero este curso <Seta /></a>
                    : <p className="text-sm text-suave">Fale com o suporte para liberar este curso.</p>)}
                </div>
              </div>
              <div className="relative">
                <div className="absolute -bottom-3 -right-3 inset-0 rounded-2xl bg-destaque" aria-hidden />
                <div className="relative aspect-[4/3] overflow-hidden rounded-2xl border border-linha bg-painel">
                  {c.capaUrl
                    ? <img src={midia(c.capaUrl)!} alt="" className="size-full object-cover" />
                    : <div className="faixas grid size-full place-items-center"><span className="titulo-esporte text-6xl text-white/10">{c.titulo}</span></div>}
                </div>
              </div>
            </div>
          </header>

          <main className="mx-auto max-w-7xl px-4 pt-12 sm:px-8">
            <h2 className="text-lg font-bold">Módulos</h2>
            <ol className="mt-5 grid gap-4 md:grid-cols-2">
              {c.modulos.map((m, i) => {
                const conteudo = (
                  <>
                    <span className="titulo-esporte w-14 shrink-0 text-5xl text-destaque">{String(i + 1).padStart(2, "0")}</span>
                    <div className="relative size-20 shrink-0 overflow-hidden rounded-lg bg-painel-2 sm:size-24">
                      {m.capaUrl ? <img src={midia(m.capaUrl)!} alt="" className="size-full object-cover" /> : <div className="faixas size-full" />}
                    </div>
                    <div className="min-w-0 flex-1">
                      <h3 className="titulo-esporte text-3xl">{m.titulo}</h3>
                      {m.descricao && <p className="mt-1 line-clamp-2 text-sm text-suave">{m.descricao}</p>}
                      <p className="mt-2 text-xs text-white/50">{m.totalTopicos} tópicos · {m.totalVideos} vídeos</p>
                    </div>
                    {c.liberado ? <Seta className="size-5 shrink-0 text-suave transition group-hover:translate-x-1 group-hover:text-destaque" /> : <Cadeado className="size-5 shrink-0 text-suave" />}
                  </>
                );
                const classe = "group flex items-center gap-4 rounded-xl border border-linha bg-painel p-4 transition";
                return (
                  <li key={m.id}>
                    {c.liberado
                      ? <Link to={`/curso/${c.slug}/${m.slug}`} className={`${classe} hover:border-destaque/60 hover:bg-painel-2`}>{conteudo}</Link>
                      : <div className={`${classe} opacity-60`}>{conteudo}</div>}
                  </li>
                );
              })}
            </ol>
          </main>
        </>
      )}
    </div>
  );
}
