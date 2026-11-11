package br.com.felipetrainer.areamembros.conteudo;

import br.com.felipetrainer.areamembros.conteudo.ConteudoDtos.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Painel: cursos, módulos (cards), tópicos e vídeos. As alterações devolvem o curso inteiro
 * atualizado, para o painel redesenhar a tela de uma vez.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminConteudoController {

    private final AdminConteudoService conteudo;

    public AdminConteudoController(AdminConteudoService conteudo) {
        this.conteudo = conteudo;
    }

    @PutMapping("/site")
    public SiteDto site(@Valid @RequestBody SiteForm form) {
        return conteudo.atualizarSite(form);
    }

    // ---- Cursos ----

    @GetMapping("/cursos")
    public List<AdminCursoDto> cursos() {
        return conteudo.cursos();
    }

    @PostMapping("/cursos")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminCursoDto criarCurso(@Valid @RequestBody CursoForm form) {
        return conteudo.criarCurso(form);
    }

    @PutMapping("/cursos/{id}")
    public AdminCursoDto atualizarCurso(@PathVariable Long id, @Valid @RequestBody CursoForm form) {
        return conteudo.atualizarCurso(id, form);
    }

    @DeleteMapping("/cursos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirCurso(@PathVariable Long id) {
        conteudo.excluirCurso(id);
    }

    @PostMapping(path = "/cursos/{id}/capa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminCursoDto capaCurso(@PathVariable Long id, @RequestParam("arquivo") MultipartFile arquivo) {
        return conteudo.enviarCapaCurso(id, arquivo);
    }

    @DeleteMapping("/cursos/{id}/capa")
    public AdminCursoDto removerCapaCurso(@PathVariable Long id) {
        return conteudo.removerCapaCurso(id);
    }

    // ---- Módulos ----

    @PostMapping("/cursos/{id}/modulos")
    public AdminCursoDto criarModulo(@PathVariable Long id, @Valid @RequestBody ModuloForm form) {
        return conteudo.criarModulo(id, form);
    }

    @PutMapping("/modulos/{id}")
    public AdminCursoDto atualizarModulo(@PathVariable Long id, @Valid @RequestBody ModuloForm form) {
        return conteudo.atualizarModulo(id, form);
    }

    @DeleteMapping("/modulos/{id}")
    public AdminCursoDto excluirModulo(@PathVariable Long id) {
        return conteudo.excluirModulo(id);
    }

    @PostMapping(path = "/modulos/{id}/capa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminCursoDto capaModulo(@PathVariable Long id, @RequestParam("arquivo") MultipartFile arquivo) {
        return conteudo.enviarCapaModulo(id, arquivo);
    }

    @DeleteMapping("/modulos/{id}/capa")
    public AdminCursoDto removerCapaModulo(@PathVariable Long id) {
        return conteudo.removerCapaModulo(id);
    }

    // ---- Tópicos ----

    @PostMapping("/modulos/{id}/topicos")
    public AdminCursoDto criarTopico(@PathVariable Long id, @Valid @RequestBody TopicoForm form) {
        return conteudo.criarTopico(id, form);
    }

    @PutMapping("/topicos/{id}")
    public AdminCursoDto atualizarTopico(@PathVariable Long id, @Valid @RequestBody TopicoForm form) {
        return conteudo.atualizarTopico(id, form);
    }

    @DeleteMapping("/topicos/{id}")
    public AdminCursoDto excluirTopico(@PathVariable Long id) {
        return conteudo.excluirTopico(id);
    }

    // ---- Vídeos ----

    @PostMapping("/topicos/{id}/videos")
    public AdminCursoDto criarVideo(@PathVariable Long id, @Valid @RequestBody VideoForm form) {
        return conteudo.criarVideo(id, form);
    }

    @PutMapping("/videos/{id}")
    public AdminCursoDto atualizarVideo(@PathVariable Long id, @Valid @RequestBody VideoForm form) {
        return conteudo.atualizarVideo(id, form);
    }

    @DeleteMapping("/videos/{id}")
    public AdminCursoDto excluirVideo(@PathVariable Long id) {
        return conteudo.excluirVideo(id);
    }

    @PostMapping(path = "/videos/{id}/arquivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminCursoDto arquivoVideo(@PathVariable Long id, @RequestParam("arquivo") MultipartFile arquivo) {
        return conteudo.enviarArquivoVideo(id, arquivo);
    }

    @DeleteMapping("/videos/{id}/arquivo")
    public AdminCursoDto removerArquivoVideo(@PathVariable Long id) {
        return conteudo.removerArquivoVideo(id);
    }
}
