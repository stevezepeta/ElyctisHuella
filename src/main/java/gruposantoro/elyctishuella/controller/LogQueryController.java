package gruposantoro.elyctishuella.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gruposantoro.elyctishuella.model.dto.ErrorCodeResponseDTO;
import gruposantoro.elyctishuella.model.dto.SessionResponseDTO;
import gruposantoro.elyctishuella.service.LogQueryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LogQueryController {

    private final LogQueryService logQueryService;

    /** 2.1 Endpoint para Consulta de Código Específico
     *  GET /api/error-codes/{code}
     */
    @GetMapping("/error-codes/{code}")
    public ResponseEntity<?> getByErrorCode(@PathVariable("code") String code) {
        ErrorCodeResponseDTO dto = logQueryService.getByErrorCode(code);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"message\":\"errorCode no encontrado\"}");
        }
        return ResponseEntity.ok(dto);
    }

    /** 2.2 Endpoint para Consulta de Sesión Completa
     *  GET /api/sessions/{baseCode}
     */
    @GetMapping("/sessions/{baseCode}")
    public ResponseEntity<?> getSession(@PathVariable("baseCode") String baseCode) {
        SessionResponseDTO dto = logQueryService.getSessionByBaseCode(baseCode);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"message\":\"baseCode sin eventos\"}");
        }
        return ResponseEntity.ok(dto);
    }
}
