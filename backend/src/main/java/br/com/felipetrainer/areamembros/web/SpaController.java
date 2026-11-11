package br.com.felipetrainer.areamembros.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Em produção o frontend (React) é servido pelo próprio Spring. Qualquer rota da tela
 * (ex.: /modulo/joelho-forte) devolve o index.html e o React cuida do resto.
 */
@Controller
public class SpaController {

    @GetMapping({"/", "/login", "/trocar-senha", "/conta", "/curso/**", "/admin", "/admin/**"})
    public String index() {
        return "forward:/index.html";
    }
}
