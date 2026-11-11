import { useState } from "react";
import Navbar from "../../components/Navbar";
import Cursos from "./Cursos";
import Usuarios from "./Usuarios";
import SiteConfig from "./SiteConfig";

type Aba = "cursos" | "usuarios" | "site";

/** Painel do admin: conteúdo dos cursos, usuários e configurações do site. */
export default function Painel() {
  const [aba, setAba] = useState<Aba>("cursos");
  const abas: [Aba, string][] = [["cursos", "Cursos e conteúdo"], ["usuarios", "Usuários e acessos"], ["site", "Site"]];
  return (
    <div className="min-h-dvh pb-24">
      <Navbar />
      <div className="mx-auto max-w-7xl px-4 pt-8 sm:px-8">
        <h1 className="titulo-esporte text-5xl">Painel</h1>
        <div className="mt-6 flex gap-1 overflow-x-auto border-b border-linha">
          {abas.map(([id, nome]) => (
            <button key={id} onClick={() => setAba(id)} className={`-mb-px shrink-0 border-b-2 px-4 py-2.5 text-sm font-medium ${aba === id ? "border-destaque text-white" : "border-transparent text-suave hover:text-white"}`}>
              {nome}
            </button>
          ))}
        </div>
        <div className="mt-8">
          {aba === "cursos" && <Cursos />}
          {aba === "usuarios" && <Usuarios />}
          {aba === "site" && <SiteConfig />}
        </div>
      </div>
    </div>
  );
}
