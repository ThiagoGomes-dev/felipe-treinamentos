import { useEffect, useRef, useState } from "react";
import { Link, NavLink } from "react-router";
import { useAuth } from "../auth";

export function Marca({ grande = false }: { grande?: boolean }) {
  return (
    <Link to="/" className="flex items-center gap-2.5">
      <span className={`grid place-items-center rounded-md bg-destaque text-sobre-destaque ${grande ? "size-11" : "size-8"}`}>
        <svg viewBox="0 0 64 64" className={grande ? "size-7" : "size-5"} aria-hidden><path d="M22 48 30 16h20l-2 8H38l-2 7h10l-2 8H34l-2 9z" fill="currentColor" /></svg>
      </span>
      <span className={`titulo-esporte ${grande ? "text-3xl" : "text-xl"}`}>Felipe <span className="text-destaque">Trainer</span></span>
    </Link>
  );
}

export default function Navbar() {
  const { usuario, sair } = useAuth();
  const [menu, setMenu] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const fechar = (e: MouseEvent) => { if (!ref.current?.contains(e.target as Node)) setMenu(false); };
    document.addEventListener("click", fechar);
    return () => document.removeEventListener("click", fechar);
  }, []);

  const link = ({ isActive }: { isActive: boolean }) =>
    `relative py-5 text-sm font-medium transition ${isActive ? "text-white after:absolute after:inset-x-0 after:bottom-0 after:h-0.5 after:bg-destaque" : "text-suave hover:text-white"}`;

  return (
    <header className="sticky top-0 z-40 border-b border-linha bg-fundo/90 backdrop-blur">
      <nav className="mx-auto flex max-w-7xl items-center gap-6 px-4 sm:gap-10 sm:px-8">
        <Marca />
        <div className="flex items-center gap-5 sm:gap-7">
          <NavLink to="/" end className={link}>Meus cursos</NavLink>
          {usuario?.papel === "ADMIN" && <NavLink to="/admin" className={link}>Painel</NavLink>}
        </div>
        <div ref={ref} className="relative ml-auto">
          <button onClick={() => setMenu((m) => !m)} className="flex items-center gap-2 rounded-full py-1 pl-1 pr-3 text-sm text-suave hover:bg-white/5 hover:text-white" aria-label="Minha conta">
            <span className="grid size-8 place-items-center rounded-full bg-painel-2 font-semibold uppercase text-white ring-1 ring-linha">{usuario?.nome?.[0] ?? "?"}</span>
            <span className="hidden max-w-32 truncate sm:inline">{usuario?.nome.split(" ")[0]}</span>
          </button>
          {menu && (
            <div className="absolute right-0 mt-2 w-60 overflow-hidden rounded-xl border border-linha bg-painel shadow-2xl">
              <div className="border-b border-linha px-4 py-3">
                <p className="truncate text-sm font-semibold">{usuario?.nome}</p>
                <p className="truncate text-xs text-suave">{usuario?.email}</p>
              </div>
              <Link to="/conta" className="block px-4 py-2.5 text-sm text-suave hover:bg-white/5 hover:text-white">Alterar senha</Link>
              <button onClick={sair} className="block w-full px-4 py-2.5 text-left text-sm text-suave hover:bg-white/5 hover:text-white">Sair</button>
            </div>
          )}
        </div>
      </nav>
    </header>
  );
}
