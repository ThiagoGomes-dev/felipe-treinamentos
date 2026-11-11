import { useCallback, useRef, useState, type ReactNode } from "react";
import { midia, tamanhoLegivel } from "../../api";
import { Lixeira, Upload } from "../../components/Icones";

export function Caixa({ children, className = "" }: { children: ReactNode; className?: string }) {
  return <div className={`rounded-xl border border-linha bg-painel p-5 ${className}`}>{children}</div>;
}

export function Campo({ rotulo, children, className = "", dica }: { rotulo: string; children: ReactNode; className?: string; dica?: string }) {
  return (
    <label className={`block ${className}`}>
      <span className="mb-1.5 block text-xs font-medium text-suave">{rotulo}</span>
      {children}
      {dica && <span className="mt-1 block text-[11px] text-suave/80">{dica}</span>}
    </label>
  );
}

/** Mensagem rápida no rodapé ("Salvo", "Erro..."). */
export function useAviso() {
  const [msg, setMsg] = useState<{ t: string; erro?: boolean } | null>(null);
  const timer = useRef<number | undefined>(undefined);
  const avisar = useCallback((t: string, erro = false) => {
    setMsg({ t, erro });
    window.clearTimeout(timer.current);
    timer.current = window.setTimeout(() => setMsg(null), erro ? 5000 : 2500);
  }, []);
  const el = msg && (
    <div role="status" className={`fixed bottom-6 left-1/2 z-50 -translate-x-1/2 rounded-lg px-5 py-3 text-sm font-semibold shadow-2xl ${msg.erro ? "bg-red-600 text-white" : "bg-destaque text-sobre-destaque"}`}>{msg.t}</div>
  );
  return { avisar, el };
}

function Progresso({ p }: { p: number }) {
  return (
    <div className="w-full">
      <div className="h-1.5 overflow-hidden rounded-full bg-fundo"><div className="h-full bg-destaque transition-all" style={{ width: `${p}%` }} /></div>
      <p className="mt-1 text-[11px] text-suave">Enviando... {p}%</p>
    </div>
  );
}

/** Imagem com prévia: enviar, trocar ou excluir. */
export function ImagemUpload({ url, aoEnviar, aoRemover, formato = "16/9", rotulo }: {
  url: string | null;
  aoEnviar: (f: File, progresso: (p: number) => void) => Promise<void>;
  aoRemover: () => Promise<void>;
  formato?: string;
  rotulo: string;
}) {
  const input = useRef<HTMLInputElement>(null);
  const [p, setP] = useState<number | null>(null);
  const escolher = async (f?: File) => {
    if (!f) return;
    setP(0);
    try { await aoEnviar(f, setP); } finally { setP(null); if (input.current) input.current.value = ""; }
  };
  return (
    <div>
      <span className="mb-1.5 block text-xs font-medium text-suave">{rotulo}</span>
      <div className="flex items-start gap-4">
        <div className="relative w-40 shrink-0 overflow-hidden rounded-lg border border-linha bg-fundo" style={{ aspectRatio: formato }}>
          {url ? <img src={midia(url)!} alt="" className="size-full object-cover" /> : <div className="faixas grid size-full place-items-center text-[11px] text-suave">Sem imagem</div>}
        </div>
        <div className="flex min-w-0 flex-1 flex-col gap-2">
          <input ref={input} type="file" accept="image/jpeg,image/png,image/webp,image/gif,image/avif" className="hidden" onChange={(e) => escolher(e.target.files?.[0])} />
          {p !== null ? <Progresso p={p} /> : (
            <div className="flex flex-wrap gap-2">
              <button type="button" className="botao-mini" onClick={() => input.current?.click()}><Upload className="size-3.5" />{url ? "Trocar imagem" : "Enviar imagem"}</button>
              {url && <button type="button" className="botao-perigo" onClick={() => confirm("Excluir esta imagem do servidor?") && aoRemover()}><Lixeira className="size-3.5" />Excluir</button>}
            </div>
          )}
          <p className="text-[11px] text-suave">JPG, PNG ou WEBP, até 15 MB.</p>
        </div>
      </div>
    </div>
  );
}

/** Arquivo de vídeo: enviar com barra de progresso, ver nome e tamanho, excluir. */
export function VideoUpload({ nome, tamanho, aoEnviar, aoRemover }: {
  nome: string | null;
  tamanho: number | null;
  aoEnviar: (f: File, progresso: (p: number) => void) => Promise<void>;
  aoRemover: () => Promise<void>;
}) {
  const input = useRef<HTMLInputElement>(null);
  const [p, setP] = useState<number | null>(null);
  const escolher = async (f?: File) => {
    if (!f) return;
    setP(0);
    try { await aoEnviar(f, setP); } finally { setP(null); if (input.current) input.current.value = ""; }
  };
  return (
    <div className="flex min-h-9 items-center gap-2">
      <input ref={input} type="file" accept="video/mp4,video/webm,video/quicktime,.mp4,.webm,.mov" className="hidden" onChange={(e) => escolher(e.target.files?.[0])} />
      {p !== null ? <Progresso p={p} /> : nome ? (
        <>
          <span className="etiqueta max-w-[14rem] truncate bg-destaque/15 normal-case tracking-normal text-destaque" title={nome}>🎬 {nome}</span>
          {tamanho != null && <span className="text-[11px] text-suave">{tamanhoLegivel(tamanho)}</span>}
          <button type="button" className="botao-mini" onClick={() => input.current?.click()}>Trocar</button>
          <button type="button" className="botao-perigo" onClick={() => confirm("Excluir este vídeo do servidor?") && aoRemover()}><Lixeira className="size-3.5" /></button>
        </>
      ) : (
        <button type="button" className="botao-mini" onClick={() => input.current?.click()}><Upload className="size-3.5" />Enviar vídeo</button>
      )}
    </div>
  );
}
