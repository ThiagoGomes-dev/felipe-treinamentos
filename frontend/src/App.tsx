import { useEffect, type ReactNode } from "react";
import { Navigate, Route, Routes, useLocation } from "react-router";
import { api, aplicarCor, type Site } from "./api";
import { useAuth } from "./auth";
import Login from "./pages/Login";
import TrocarSenha from "./pages/TrocarSenha";
import MeusCursos from "./pages/MeusCursos";
import CursoPage from "./pages/Curso";
import Aula from "./pages/Aula";
import Painel from "./pages/admin/Painel";

function Protegido({ children, admin = false }: { children: ReactNode; admin?: boolean }) {
  const { usuario, carregando } = useAuth();
  const local = useLocation();
  if (carregando) return <div className="min-h-dvh" />;
  if (!usuario) return <Navigate to="/login" replace state={{ de: local.pathname }} />;
  // Senha provisória: obriga a criar uma senha nova antes de ver o conteúdo.
  if (usuario.senhaProvisoria && local.pathname !== "/trocar-senha") return <Navigate to="/trocar-senha" replace />;
  if (admin && usuario.papel !== "ADMIN") return <Navigate to="/" replace />;
  return <>{children}</>;
}

export default function App() {
  useEffect(() => {
    api.get<Site>("/site").then((s) => { aplicarCor(s.corDestaque); document.title = s.nome; }).catch(() => {});
  }, []);

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/trocar-senha" element={<Protegido><TrocarSenha /></Protegido>} />
      <Route path="/conta" element={<Protegido><TrocarSenha conta /></Protegido>} />
      <Route path="/" element={<Protegido><MeusCursos /></Protegido>} />
      <Route path="/curso/:curso" element={<Protegido><CursoPage /></Protegido>} />
      <Route path="/curso/:curso/:modulo" element={<Protegido><Aula /></Protegido>} />
      <Route path="/admin" element={<Protegido admin><Painel /></Protegido>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
