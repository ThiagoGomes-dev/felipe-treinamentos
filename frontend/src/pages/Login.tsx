import { useEffect, useState, type FormEvent } from "react";
import { Navigate, useLocation, useNavigate } from "react-router";
import { api, type Site } from "../api";
import { useAuth } from "../auth";
import { Marca } from "../components/Navbar";

export default function Login() {
  const { usuario, entrar } = useAuth();
  const nav = useNavigate();
  const local = useLocation();
  const [site, setSite] = useState<Site | null>(null);
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);

  useEffect(() => { api.get<Site>("/site").then(setSite).catch(() => {}); }, []);

  if (usuario) return <Navigate to="/" replace />;

  async function enviar(e: FormEvent) {
    e.preventDefault();
    setErro("");
    setEnviando(true);
    try {
      const u = await entrar(email, senha);
      const destino = (local.state as { de?: string } | null)?.de ?? "/";
      nav(u.senhaProvisoria ? "/trocar-senha" : destino, { replace: true });
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="grid min-h-dvh lg:grid-cols-2">
      <div className="faixas relative hidden flex-col justify-between overflow-hidden border-r border-linha bg-painel p-12 lg:flex">
        <Marca grande />
        <div>
          <p className="titulo-esporte text-[7.5rem] text-white">Treina<br /><span className="text-destaque">forte.</span></p>
          {site?.slogan && <p className="mt-6 max-w-md text-lg text-white/70">{site.slogan}</p>}
        </div>
        <p className="text-xs text-suave">© {new Date().getFullYear()} {site?.nome ?? "Felipe Trainer"}</p>
      </div>

      <div className="flex items-center justify-center px-4 py-12 sm:px-10">
        <form onSubmit={enviar} className="w-full max-w-sm">
          <div className="lg:hidden"><Marca grande /></div>
          <h1 className="mt-10 text-3xl font-bold lg:mt-0">Entrar</h1>
          <p className="mt-2 text-sm text-suave">Use o e-mail da sua compra. No primeiro acesso, a senha é a sua data de nascimento, só os números (ddmmaaaa).</p>

          <label className="mt-8 block text-sm font-medium">E-mail</label>
          <input className="campo mt-2" type="email" autoComplete="email" required value={email} onChange={(e) => setEmail(e.target.value)} placeholder="voce@email.com" />

          <label className="mt-5 block text-sm font-medium">Senha</label>
          <input className="campo mt-2" type="password" autoComplete="current-password" required value={senha} onChange={(e) => setSenha(e.target.value)} placeholder="••••••••" />

          {erro && <p className="mt-4 rounded-lg bg-red-500/10 px-3 py-2 text-sm text-red-300">{erro}</p>}

          <button className="botao mt-8 w-full" disabled={enviando}>{enviando ? "Entrando..." : "Entrar"}</button>
        </form>
      </div>
    </div>
  );
}
