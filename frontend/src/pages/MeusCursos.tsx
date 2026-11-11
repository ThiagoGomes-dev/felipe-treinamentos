import { useEffect, useState } from "react";
import { Link } from "react-router";
import { api, midia, type CursoCard } from "../api";
import { useAuth } from "../auth";
import Navbar from "../components/Navbar";
import { Cadeado, Seta } from "../components/Icones";

function Capa({ url, titulo, className = "" }: { url: string | null; titulo: string; className?: string }) {
  return url
    ? <img src={midia(url)!} alt="" className={`object-cover ${className}`} />
    : (
      <div className={`faixas grid place-items-center bg-painel-2 ${className}`}>
        <span className="titulo-esporte px-6 text-center text-4xl text-white/15">{titulo}</span>
      </div>
    );
}

export default function MeusCursos() {
  const { usuario } = useAuth();
  const [cursos, setCursos] = useState<CursoCard[] | null>(null);
  const [erro, setErro] = useState("");

  useEffect(() => { api.get<CursoCard[]>("/cursos").then(setCursos).catch((e) => setErro(e.message)); }, []);

  const meus = cursos?.filter((c) => c.liberado) ?? [];
  const outros = cursos?.filter((c) => !c.liberado) ?? [];
  const destaque = meus[0];

  return (
    <div className="min-h-dvh pb-24">
      <Navbar />
      <main className="mx-auto max-w-7xl px-4 pt-10 sm:px-8">
        <p className="text-sm font-medium text-suave">Bom treino,</p>
        <h1 className="titulo-esporte mt-1 text-5xl sm:text-6xl">{usuario?.nome.split(" ")[0]}<span className="text-destaque">.</span></h1>

        {erro && <p className="mt-6 text-sm text-red-300">{erro}</p>}

        {/* Destaque: primeiro curso liberado */}
        {destaque && (
          <Link to={`/curso/${destaque.slug}`} className="group mt-8 grid overflow-hidden rounded-2xl border border-linha bg-painel transition hover:border-destaque/60 md:grid-cols-[1.1fr_1fr]">
            <div className="flex flex-col justify-center gap-4 p-6 sm:p-10">
              <span className="etiqueta w-fit bg-destaque text-sobre-destaque">Seu programa</span>
              <h2 className="titulo-esporte text-5xl sm:text-6xl">{destaque.titulo}</h2>
              {destaque.subtitulo && <p className="text-lg text-white/80">{destaque.subtitulo}</p>}
              <p className="text-sm text-suave">{destaque.totalModulos} módulos · {destaque.totalVideos} vídeos</p>
              <span className="botao mt-2 w-fit">Continuar treinando <Seta /></span>
            </div>
            <div className="relative min-h-56 md:min-h-80">
              <Capa url={destaque.capaUrl} titulo={destaque.titulo} className="absolute inset-0 size-full transition duration-500 group-hover:scale-[1.03]" />
              <div className="absolute inset-y-0 left-0 hidden w-24 bg-gradient-to-r from-painel to-transparent md:block" />
            </div>
          </Link>
        )}

        {meus.length > 1 && (
          <section className="mt-14">
            <h2 className="text-lg font-bold">Seus cursos</h2>
            <div className="mt-5 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
              {meus.slice(1).map((c) => <CardCurso key={c.id} c={c} />)}
            </div>
          </section>
        )}

        {cursos && meus.length === 0 && (
          <div className="mt-10 rounded-2xl border border-dashed border-linha p-10 text-center text-suave">
            Nenhum curso liberado na sua conta ainda. Se você já comprou, fale com o suporte.
          </div>
        )}

        {outros.length > 0 && (
          <section className="mt-14">
            <h2 className="text-lg font-bold">Outros cursos</h2>
            <p className="mt-1 text-sm text-suave">Ainda não estão liberados para você.</p>
            <div className="mt-5 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
              {outros.map((c) => <CardCurso key={c.id} c={c} />)}
            </div>
          </section>
        )}
      </main>
    </div>
  );
}

function CardCurso({ c }: { c: CursoCard }) {
  return (
    <Link to={`/curso/${c.slug}`} className="group flex flex-col overflow-hidden rounded-xl border border-linha bg-painel transition hover:-translate-y-0.5 hover:border-white/25">
      <div className="relative aspect-[16/9] overflow-hidden">
        <Capa url={c.capaUrl} titulo={c.titulo} className={`absolute inset-0 size-full ${c.liberado ? "" : "grayscale opacity-60"}`} />
        {!c.liberado && <span className="etiqueta absolute left-3 top-3 bg-black/70 text-white"><Cadeado className="size-3" /> Bloqueado</span>}
      </div>
      <div className="flex flex-1 flex-col p-5">
        <h3 className="titulo-esporte text-3xl">{c.titulo}</h3>
        {c.subtitulo && <p className="mt-2 line-clamp-2 text-sm text-white/70">{c.subtitulo}</p>}
        <div className="mt-auto flex items-center justify-between pt-5 text-sm">
          <span className="text-suave">{c.totalModulos} módulos · {c.totalVideos} vídeos</span>
          <span className="flex items-center gap-1 font-semibold text-destaque">{c.liberado ? "Acessar" : "Conhecer"} <Seta /></span>
        </div>
      </div>
    </Link>
  );
}
