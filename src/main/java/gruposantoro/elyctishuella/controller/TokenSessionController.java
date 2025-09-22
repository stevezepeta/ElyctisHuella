package gruposantoro.elyctishuella.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gruposantoro.elyctishuella.domain.TokenSessionService;
import gruposantoro.elyctishuella.model.dto.LogDTO;
import gruposantoro.elyctishuella.model.dto.TokenSessionResponse;

@RestController
@RequestMapping("/api/tokenSession")
public class TokenSessionController {

    private final TokenSessionService service;

    public TokenSessionController(TokenSessionService service) {
        this.service = service;
    }

    @GetMapping("/{sessionToken}")
    public ResponseEntity<List<LogDTO>> getBySessionToken(@PathVariable String sessionToken) {
        TokenSessionResponse response = service.getLogsBySessionToken(sessionToken);
        return ResponseEntity.ok(response.data()); // devolvemos solo el arreglo
    }
}
