import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react";
import { api, token, type Usuario } from "./api";

interface AuthCtx {
  usuario: Usuario | null;
  carregando: boolean;
  entrar: (email: string, senha: string) => Promise<Usuario>;
  sair: () => void;
  atualizar: (u: Usuario) => void;
}

const Ctx = createContext<AuthCtx | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [carregando, setCarregando] = useState(true);

  const sair = useCallback(() => {
    token.set(null);
    setUsuario(null);
  }, []);

  useEffect(() => {
    if (!token.get()) {
      setCarregando(false);
      return;
    }
    api.get<Usuario>("/auth/me")
      .then(setUsuario)
      .catch(() => token.set(null))
      .finally(() => setCarregando(false));
  }, []);

  useEffect(() => {
    window.addEventListener("am:sair", sair);
    return () => window.removeEventListener("am:sair", sair);
  }, [sair]);

  const entrar = async (email: string, senha: string) => {
    const r = await api.post<{ token: string; usuario: Usuario }>("/auth/login", { email, senha });
    token.set(r.token);
    setUsuario(r.usuario);
    return r.usuario;
  };

  return (
    <Ctx.Provider value={{ usuario, carregando, entrar, sair, atualizar: setUsuario }}>
      {children}
    </Ctx.Provider>
  );
}

export function useAuth() {
  const c = useContext(Ctx);
  if (!c) throw new Error("useAuth fora do AuthProvider");
  return c;
}
