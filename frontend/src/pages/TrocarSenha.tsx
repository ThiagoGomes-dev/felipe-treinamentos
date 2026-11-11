import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router";
import { api, type Usuario } from "../api";
import { useAuth } from "../auth";

/** Primeiro acesso (senha provisória) ou "Alterar senha" no menu da conta. */
export default function TrocarSenha({ conta = false }: { conta?: boolean }) {
  const { usuario, atualizar, sair } = useAuth();
  const nav = useNavigate();
  const [atual, setAtual] = useState("");
  const [nova, setNova] = useState("");
  const [confirma, setConfirma] = useState("");
  const [erro, setErro] = useState("");
  const [ok, setOk] = useState(false);

  async function enviar(e: FormEvent) {
    e.preventDefault();
    setErro("");
    if (nova !== confirma) return setErro("As senhas não são iguais.");
    try {
      const u = await api.post<Usuario>("/auth/trocar-senha", { senhaAtual: atual, novaSenha: nova });
      atualizar(u);
      if (conta) { setOk(true); setAtual(""); setNova(""); setConfirma(""); }
      else nav("/", { replace: true });
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  return (
    <div className="faixas flex min-h-dvh items-center justify-center px-4 py-12">
      <form onSubmit={enviar} className="w-full max-w-sm rounded-2xl border border-linha bg-painel p-6 sm:p-8">
        <h1 className="text-2xl font-bold">{conta ? "Alterar senha" : `Bem-vindo, ${usuario?.nome.split(" ")[0]}!`}</h1>
        <p className="mt-2 text-sm text-suave">
          {conta ? "Escolha uma senha nova para sua conta." : "Antes de começar, crie uma senha só sua. A senha atual é a provisória que chegou no seu e-mail."}
        </p>
        <label className="mt-6 block text-sm font-medium">Senha atual</label>
        <input className="campo mt-2" type="password" required value={atual} onChange={(e) => setAtual(e.target.value)} autoComplete="current-password" />
        <label className="mt-4 block text-sm font-medium">Nova senha</label>
        <input className="campo mt-2" type="password" required minLength={6} value={nova} onChange={(e) => setNova(e.target.value)} autoComplete="new-password" />
        <label className="mt-4 block text-sm font-medium">Confirme a nova senha</label>
        <input className="campo mt-2" type="password" required minLength={6} value={confirma} onChange={(e) => setConfirma(e.target.value)} autoComplete="new-password" />
        {erro && <p className="mt-4 rounded-lg bg-red-500/10 px-3 py-2 text-sm text-red-300">{erro}</p>}
        {ok && <p className="mt-4 rounded-lg bg-emerald-500/10 px-3 py-2 text-sm text-emerald-300">Senha alterada.</p>}
        <button className="botao mt-6 w-full">Salvar senha</button>
        <div className="mt-4 text-center text-sm text-suave">
          {conta ? <Link to="/" className="hover:text-white">Voltar para o início</Link> : <button type="button" onClick={sair} className="hover:text-white">Sair</button>}
        </div>
      </form>
    </div>
  );
}
