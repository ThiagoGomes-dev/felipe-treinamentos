import { useCallback, useEffect, useMemo, useState, type FormEvent, type ReactNode } from "react";
import { api, type AdminCurso, type Papel, type UsuarioAdmin } from "../../api";
import { useAuth } from "../../auth";
import { Lixeira, Mais } from "../../components/Icones";
import { Campo, useAviso } from "./ui";

type CursoMin = { id: number; titulo: string };

/** Controle de usuários: quem é admin, quem está bloqueado e quais cursos cada um pode ver. */
export default function Usuarios() {
  const [usuarios, setUsuarios] = useState<UsuarioAdmin[]>([]);
  const [cursos, setCursos] = useState<CursoMin[]>([]);
  const [busca, setBusca] = useState("");
  const [filtro, setFiltro] = useState<"todos" | Papel | "bloqueados">("todos");
  const [editando, setEditando] = useState<UsuarioAdmin | null>(null);
  const [novo, setNovo] = useState(false);
  const { avisar, el } = useAviso();

  const carregar = useCallback(async () => {
    const [us, cs] = await Promise.all([api.get<UsuarioAdmin[]>("/admin/usuarios"), api.get<AdminCurso[]>("/admin/cursos")]);
    setUsuarios(us);
    setCursos(cs.map((c) => ({ id: c.id, titulo: c.titulo })));
  }, []);
  useEffect(() => { carregar().catch((e) => avisar(e.message, true)); }, [carregar, avisar]);

  const visiveis = useMemo(() => usuarios.filter((u) => {
    const b = busca.trim().toLowerCase();
    if (b && !u.nome.toLowerCase().includes(b) && !u.email.includes(b)) return false;
    if (filtro === "bloqueados") return !u.ativo;
    return filtro === "todos" || u.papel === filtro;
  }), [usuarios, busca, filtro]);

  const nomeCurso = (id: number) => cursos.find((c) => c.id === id)?.titulo ?? "?";

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center gap-3">
        <input className="campo max-w-xs" placeholder="Buscar por nome ou e-mail" value={busca} onChange={(e) => setBusca(e.target.value)} />
        <select className="campo w-auto" value={filtro} onChange={(e) => setFiltro(e.target.value as typeof filtro)}>
          <option value="todos">Todos</option>
          <option value="ALUNO">Alunos</option>
          <option value="ADMIN">Admins</option>
          <option value="bloqueados">Bloqueados</option>
        </select>
        <span className="text-sm text-suave">{visiveis.length} de {usuarios.length}</span>
        <button className="botao ml-auto !py-2.5" onClick={() => setNovo(true)}><Mais />Novo usuário</button>
      </div>

      <div className="overflow-x-auto rounded-xl border border-linha">
        <table className="w-full min-w-[760px] text-left text-sm">
          <thead className="bg-painel text-xs uppercase tracking-wider text-suave">
            <tr><th className="px-4 py-3">Usuário</th><th className="px-4 py-3">Tipo</th><th className="px-4 py-3">Cursos liberados</th><th className="px-4 py-3">Situação</th><th className="px-4 py-3" /></tr>
          </thead>
          <tbody className="divide-y divide-linha">
            {visiveis.length === 0 && <tr><td colSpan={5} className="px-4 py-8 text-center text-suave">Nenhum usuário encontrado.</td></tr>}
            {visiveis.map((u) => (
              <tr key={u.id} className="hover:bg-white/[0.02]">
                <td className="px-4 py-3">
                  <p className="font-medium">{u.nome}</p>
                  <p className="text-xs text-suave">{u.email} · {u.origem === "EDUZZ" ? "Eduzz" : "Manual"} · {new Date(u.criadoEm).toLocaleDateString("pt-BR")}</p>
                </td>
                <td className="px-4 py-3">{u.papel === "ADMIN" ? <span className="etiqueta bg-destaque text-sobre-destaque">Admin</span> : <span className="etiqueta bg-painel-2 text-white/80">Aluno</span>}</td>
                <td className="px-4 py-3">
                  {u.papel === "ADMIN" ? <span className="text-xs text-suave">Todos (admin)</span>
                    : u.cursos.length === 0 ? <span className="text-xs text-suave">Nenhum</span>
                    : <div className="flex flex-wrap gap-1">{u.cursos.map((id) => <span key={id} className="etiqueta bg-painel-2 normal-case tracking-normal text-white/80">{nomeCurso(id)}</span>)}</div>}
                </td>
                <td className="px-4 py-3">
                  {!u.ativo ? <span className="etiqueta bg-red-500/15 text-red-300">Bloqueado</span>
                    : u.senhaProvisoria ? <span className="etiqueta bg-yellow-500/15 text-yellow-300">Não entrou ainda</span>
                    : <span className="etiqueta bg-emerald-500/15 text-emerald-300">Ativo</span>}
                </td>
                <td className="px-4 py-3 text-right"><button className="botao-mini" onClick={() => setEditando(u)}>Gerenciar</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {novo && <NovoUsuario cursos={cursos} fechar={() => setNovo(false)} pronto={async (msg) => { setNovo(false); await carregar(); avisar(msg); }} erro={(m) => avisar(m, true)} />}
      {editando && <EditarUsuario u={editando} cursos={cursos} fechar={() => setEditando(null)} pronto={async (msg) => { setEditando(null); await carregar(); avisar(msg); }} erro={(m) => avisar(m, true)} />}
      {el}
    </div>
  );
}

function Gaveta({ titulo, fechar, children }: { titulo: string; fechar: () => void; children: ReactNode }) {
  return (
    <div className="fixed inset-0 z-50 flex justify-end bg-black/60" onClick={fechar}>
      <div className="h-full w-full max-w-md overflow-y-auto border-l border-linha bg-painel p-6" onClick={(e) => e.stopPropagation()}>
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-bold">{titulo}</h2>
          <button onClick={fechar} className="grid size-8 place-items-center rounded-md text-suave hover:bg-white/5 hover:text-white" aria-label="Fechar">✕</button>
        </div>
        <div className="mt-6">{children}</div>
      </div>
    </div>
  );
}

function EscolherCursos({ cursos, marcados, mudar, desativado }: { cursos: CursoMin[]; marcados: number[]; mudar: (ids: number[]) => void; desativado?: boolean }) {
  return (
    <div className="space-y-2">
      {cursos.map((c) => (
        <label key={c.id} className={`flex items-center gap-3 rounded-lg border border-linha px-3 py-2.5 text-sm ${desativado ? "opacity-50" : "cursor-pointer hover:border-white/25"}`}>
          <input type="checkbox" disabled={desativado} className="size-4 accent-[var(--destaque)]" checked={marcados.includes(c.id)}
            onChange={(e) => mudar(e.target.checked ? [...marcados, c.id] : marcados.filter((x) => x !== c.id))} />
          {c.titulo}
        </label>
      ))}
      {cursos.length === 0 && <p className="text-sm text-suave">Nenhum curso cadastrado.</p>}
    </div>
  );
}

type Retorno = { fechar: () => void; pronto: (msg: string) => Promise<void>; erro: (m: string) => void };

function NovoUsuario({ cursos, fechar, pronto, erro }: { cursos: CursoMin[] } & Retorno) {
  const [f, setF] = useState({ nome: "", email: "", dataNascimento: "", papel: "ALUNO" as Papel, cursos: [] as number[] });
  const salvar = async (e: FormEvent) => {
    e.preventDefault();
    try {
      await api.post("/admin/usuarios", { ...f, dataNascimento: f.dataNascimento || null, cursos: f.papel === "ADMIN" ? [] : f.cursos });
      await pronto("Usuário criado e e-mail de acesso enviado");
    } catch (e) { erro((e as Error).message); }
  };
  return (
    <Gaveta titulo="Novo usuário" fechar={fechar}>
      <form onSubmit={salvar} className="grid gap-4">
        <Campo rotulo="Nome"><input className="campo" required value={f.nome} onChange={(e) => setF({ ...f, nome: e.target.value })} /></Campo>
        <Campo rotulo="E-mail (login)"><input className="campo" required type="email" value={f.email} onChange={(e) => setF({ ...f, email: e.target.value })} /></Campo>
        <Campo rotulo="Data de nascimento" dica="Vira a senha provisória (ddmmaaaa). Sem data, o sistema gera uma senha e manda por e-mail.">
          <input className="campo" type="date" value={f.dataNascimento} onChange={(e) => setF({ ...f, dataNascimento: e.target.value })} />
        </Campo>
        <Campo rotulo="Tipo de usuário">
          <select className="campo" value={f.papel} onChange={(e) => setF({ ...f, papel: e.target.value as Papel })}>
            <option value="ALUNO">Aluno: vê os cursos liberados</option>
            <option value="ADMIN">Admin: cadastra cursos, vídeos e usuários</option>
          </select>
        </Campo>
        <div>
          <p className="mb-2 text-xs font-medium text-suave">Cursos liberados</p>
          <EscolherCursos cursos={cursos} marcados={f.cursos} mudar={(ids) => setF({ ...f, cursos: ids })} desativado={f.papel === "ADMIN"} />
        </div>
        <button className="botao mt-2">Criar e enviar acesso</button>
      </form>
    </Gaveta>
  );
}

function EditarUsuario({ u, cursos, fechar, pronto, erro }: { u: UsuarioAdmin; cursos: CursoMin[] } & Retorno) {
  const { usuario: eu } = useAuth();
  const souEu = eu?.id === u.id;
  const [f, setF] = useState({ nome: u.nome, dataNascimento: u.dataNascimento ?? "", papel: u.papel, ativo: u.ativo });
  const [marcados, setMarcados] = useState<number[]>(u.cursos);

  const salvar = async (e: FormEvent) => {
    e.preventDefault();
    try {
      await api.put(`/admin/usuarios/${u.id}`, { ...f, dataNascimento: f.dataNascimento || null });
      if (f.papel === "ALUNO") await api.put(`/admin/usuarios/${u.id}/cursos`, { cursos: marcados });
      await pronto("Usuário salvo");
    } catch (e) { erro((e as Error).message); }
  };
  const reenviar = async () => {
    if (!confirm("A senha volta a ser a provisória e o e-mail de acesso é enviado de novo. Continuar?")) return;
    try { await api.post(`/admin/usuarios/${u.id}/reenviar-acesso`); await pronto("Acesso reenviado"); } catch (e) { erro((e as Error).message); }
  };
  const excluir = async () => {
    if (!confirm(`Excluir ${u.nome}? Essa ação não pode ser desfeita.`)) return;
    try { await api.del(`/admin/usuarios/${u.id}`); await pronto("Usuário excluído"); } catch (e) { erro((e as Error).message); }
  };

  return (
    <Gaveta titulo="Gerenciar usuário" fechar={fechar}>
      <p className="-mt-3 mb-6 text-sm text-suave">{u.email}</p>
      <form onSubmit={salvar} className="grid gap-4">
        <Campo rotulo="Nome"><input className="campo" required value={f.nome} onChange={(e) => setF({ ...f, nome: e.target.value })} /></Campo>
        <Campo rotulo="Data de nascimento"><input className="campo" type="date" value={f.dataNascimento} onChange={(e) => setF({ ...f, dataNascimento: e.target.value })} /></Campo>
        <Campo rotulo="Tipo de usuário">
          <select className="campo" value={f.papel} disabled={souEu} onChange={(e) => setF({ ...f, papel: e.target.value as Papel })}>
            <option value="ALUNO">Aluno</option>
            <option value="ADMIN">Admin</option>
          </select>
        </Campo>
        <label className={`flex items-center justify-between rounded-lg border border-linha px-3 py-3 text-sm ${souEu ? "opacity-50" : ""}`}>
          <span><span className="font-medium">Acesso ativo</span><span className="block text-xs text-suave">Desligado, a pessoa não consegue entrar.</span></span>
          <input type="checkbox" disabled={souEu} className="size-5 accent-[var(--destaque)]" checked={f.ativo} onChange={(e) => setF({ ...f, ativo: e.target.checked })} />
        </label>
        <div>
          <p className="mb-2 text-xs font-medium text-suave">Cursos liberados {f.papel === "ADMIN" && "(admin vê todos)"}</p>
          <EscolherCursos cursos={cursos} marcados={marcados} mudar={setMarcados} desativado={f.papel === "ADMIN"} />
        </div>
        <button className="botao mt-2">Salvar alterações</button>
      </form>
      <div className="mt-8 space-y-2 border-t border-linha pt-6">
        <button className="botao-sec w-full" onClick={reenviar}>Reenviar e-mail de acesso</button>
        {!souEu && <button className="botao-perigo w-full justify-center py-2.5" onClick={excluir}><Lixeira className="size-3.5" />Excluir usuário</button>}
      </div>
    </Gaveta>
  );
}
