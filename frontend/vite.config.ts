import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

// A Vercel define VERCEL=1 durante o build.
declare const process: { env: Record<string, string | undefined> };

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    // Em desenvolvimento, as chamadas /api vão para o Spring Boot.
    proxy: { "/api": "http://localhost:8080" },
  },
  build: {
    // Na Vercel o site sai em dist/. Fora dela, o build vai para dentro do backend,
    // que serve o site junto com a API (Docker).
    outDir: process.env.VERCEL ? "dist" : "../backend/src/main/resources/static",
    emptyOutDir: true,
  },
});
