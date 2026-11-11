-- Conteúdo do arquivo guardado no banco (usado para imagens quando o servidor não tem disco fixo).
ALTER TABLE arquivo ADD COLUMN dados BYTEA;
