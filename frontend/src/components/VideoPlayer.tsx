import { midia } from "../api";

/** Toca o vídeo enviado ao servidor, ou um link do YouTube/Vimeo/.mp4. Sem vídeo, mostra "em breve". */
type Fonte = { tipo: "iframe" | "arquivo"; src: string };

function fonte(src: string): Fonte | null {
  if (src.includes("/api/midia/")) return { tipo: "arquivo", src };
  try {
    const u = new URL(src);
    const host = u.hostname.replace(/^www\.|^m\./, "");
    if (host === "youtu.be") return { tipo: "iframe", src: `https://www.youtube-nocookie.com/embed/${u.pathname.slice(1)}?rel=0` };
    if (host === "youtube.com" || host === "youtube-nocookie.com") {
      const id = u.searchParams.get("v") ?? u.pathname.match(/\/(?:shorts|embed|live)\/([^/?]+)/)?.[1];
      if (id) return { tipo: "iframe", src: `https://www.youtube-nocookie.com/embed/${id}?rel=0` };
    }
    if (host === "vimeo.com") {
      const id = u.pathname.split("/").filter(Boolean)[0];
      if (id) return { tipo: "iframe", src: `https://player.vimeo.com/video/${id}` };
    }
    if (host === "player.vimeo.com") return { tipo: "iframe", src };
    if (/\.(mp4|webm|mov)$/i.test(u.pathname)) return { tipo: "arquivo", src };
    return { tipo: "iframe", src };
  } catch {
    return null;
  }
}

export default function VideoPlayer({ src, titulo }: { src: string | null; titulo: string }) {
  const f = src ? fonte(midia(src)!) : null;
  return (
    <div className="relative aspect-video overflow-hidden rounded-xl bg-black ring-1 ring-linha">
      {!f && (
        <div className="faixas absolute inset-0 grid place-items-center bg-painel">
          <div className="text-center">
            <div className="mx-auto grid size-14 place-items-center rounded-full border border-linha bg-fundo">
              <svg viewBox="0 0 24 24" className="ml-1 size-6 fill-suave"><path d="M8 5v14l11-7z" /></svg>
            </div>
            <p className="mt-3 text-sm text-suave">Vídeo em breve</p>
          </div>
        </div>
      )}
      {f?.tipo === "iframe" && (
        <iframe key={f.src} src={f.src} title={titulo} className="absolute inset-0 size-full" allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture; fullscreen" allowFullScreen />
      )}
      {f?.tipo === "arquivo" && (
        <video key={f.src} src={f.src} controls playsInline preload="metadata" controlsList="nodownload" onContextMenu={(e) => e.preventDefault()} className="absolute inset-0 size-full bg-black" />
      )}
    </div>
  );
}
