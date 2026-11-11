import { useEffect, useState, type FormEvent } from "react";
import { api, aplicarCor, type Site } from "../../api";
import { Caixa, Campo, useAviso } from "./ui";

export default function SiteConfig() {
  const [f, setF] = useState<Site | null>(null);
  const { avisar, el } = useAviso();

  useEffect(() => { api.get<Site>("/site").then(setF); }, []);
  if (!f) return null;

  const salvar = async (e: FormEvent) => {
    e.preventDefault();
    try { const s = await api.put<Site>("/admin/site", f); aplicarCor(s.corDestaque); avisar("Site salvo"); }
    catch (e) { avisar((e as Error).message, true); }
  };

  return (
    <Caixa className="max-w-2xl">
      <form onSubmit={salvar} className="grid gap-4">
        <Campo rotulo="Nome do site"><input className="campo" required value={f.nome} onChange={(e) => setF({ ...f, nome: e.target.value })} /></Campo>
        <Campo rotulo="Frase da tela de login"><input className="campo" value={f.slogan ?? ""} onChange={(e) => setF({ ...f, slogan: e.target.value })} /></Campo>
        <Campo rotulo="Cor de destaque" dica="Usada em botões, números e detalhes. O texto por cima se ajusta sozinho (claro ou escuro).">
          <div className="flex items-center gap-3">
            <input type="color" className="h-10 w-14 cursor-pointer rounded border border-linha bg-transparent" value={f.corDestaque} onChange={(e) => { setF({ ...f, corDestaque: e.target.value }); aplicarCor(e.target.value); }} />
            <span className="text-sm text-suave">{f.corDestaque}</span>
          </div>
        </Campo>
        <div className="flex justify-end"><button className="botao">Salvar</button></div>
      </form>
      {el}
    </Caixa>
  );
}
