package br.com.felipetrainer.areamembros.conteudo;

import br.com.felipetrainer.areamembros.conteudo.ConteudoDtos.*;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/** Conteúdo que o aluno logado vê. */
@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoService catalogo;

    public CatalogoController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    /** Público: nome e cor do site (usado na tela de login). */
    @GetMapping("/site")
    public SiteDto site() {
        return catalogo.site();
    }

    @GetMapping("/cursos")
    public List<CursoCardDto> cursos() {
        return catalogo.cursos();
    }

    @GetMapping("/cursos/{slug}")
    public CursoDto curso(@PathVariable String slug) {
        return catalogo.curso(slug);
    }

    @GetMapping("/cursos/{curso}/modulos/{modulo}")
    public ModuloDto modulo(@PathVariable String curso, @PathVariable String modulo) {
        return catalogo.modulo(curso, modulo);
    }
}
