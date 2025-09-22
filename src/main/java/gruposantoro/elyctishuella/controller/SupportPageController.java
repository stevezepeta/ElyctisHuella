// src/main/java/gruposantoro/elyctishuella/controller/SupportPageController.java
package gruposantoro.elyctishuella.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SupportPageController {

    // /support/ui → forward al archivo estático (preserva ?code=...&session=...)
    @GetMapping("/support/ui")
    public String supportUi() {
        return "forward:/support/index.html";
    }

    // (opcional) /support o /support/ → también abre la UI
    @GetMapping({"/support", "/support/"})
    public String supportRoot() {
        return "forward:/support/index.html";
    }
}
