package gruposantoro.elyctishuella.controller;

import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import gruposantoro.elyctishuella.model.dto.ErrorCodeResponseDTO;
import gruposantoro.elyctishuella.model.dto.SessionResponseDTO;
import gruposantoro.elyctishuella.model.dto.support.QueryContextDTO;
import gruposantoro.elyctishuella.model.dto.support.SupportRequestDTO;
import gruposantoro.elyctishuella.model.dto.support.SupportResponseDTO;
import gruposantoro.elyctishuella.service.LogQueryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SupportJsonController {

    private final LogQueryService logQueryService;

    private static final Pattern ERROR_CODE_RX = Pattern.compile("^USR\\d{12}-ERR\\d{3}$");
    private static final Pattern BASE_CODE_RX  = Pattern.compile("^USR\\d{12}$");

    /**
     * GET /support?code=USR001241501001-ERR007&session=USR001241501001&device=PC01&user=123
     * Retorna JSON con detalle del error (obligatorio) y, si se envía, resumen de la sesión.
     * NOTA: 'code' va en QUERY PARAMS (no body). Se marca required=false para responder JSON propio si falta.
     */
    @GetMapping(value = "/support", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> supportGet(
            @RequestParam(name = "code",    required = false) String code,
            @RequestParam(name = "session", required = false) String baseCode,
            @RequestParam(name = "device",  required = false) String device,
            @RequestParam(name = "user",    required = false) String userId
    ) {
        return buildResponse(code, baseCode, device, userId);
    }

    /**
     * POST /support
     * Body JSON:
     * {
     *   "code": "USR001241501001-ERR007",
     *   "session": "USR001241501001",
     *   "device": "DESKTOP-NSAGRV5",
     *   "user": "1"
     * }
     */
    @PostMapping(value = "/support", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> supportPost(@RequestBody(required = false) SupportRequestDTO req) {
        if (req == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new SupportResponseDTO(
                            QueryContextDTO.of(null, null, null, null),
                            null, null,
                            "Body requerido en formato JSON.",
                            "bad_request"
                    ));
        }
        return buildResponse(req.getCode(), req.getSession(), req.getDevice(), req.getUser());
    }

    /* ========= Lógica compartida ========= */
    private ResponseEntity<?> buildResponse(String code, String baseCode, String device, String userId) {
        final String codeNorm     = normalizeUpper(code);
        final String baseCodeNorm = normalizeUpper(baseCode);
        final String deviceNorm   = trimOrNull(device);
        final String userNorm     = trimOrNull(userId);

        // Validación del code (obligatorio)
        if (codeNorm == null || !ERROR_CODE_RX.matcher(codeNorm).matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new SupportResponseDTO(
                            QueryContextDTO.of(codeNorm, baseCodeNorm, deviceNorm, userNorm),
                            null, null,
                            "Formato inválido para 'code'. Esperado: USR\\d{12}-ERR\\d{3}",
                            "bad_request"
                    ));
        }

        // Consulta por errorCode
        ErrorCodeResponseDTO errorDto = logQueryService.getByErrorCode(codeNorm);
        if (errorDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new SupportResponseDTO(
                            QueryContextDTO.of(codeNorm, baseCodeNorm, deviceNorm, userNorm),
                            null, null,
                            "No se encontraron eventos para el código de error.",
                            "not_found"
                    ));
        }

        // Consulta opcional de sesión
        SessionResponseDTO sessionDto = null;
        if (baseCodeNorm != null && !baseCodeNorm.isBlank()) {
            if (!BASE_CODE_RX.matcher(baseCodeNorm).matches()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new SupportResponseDTO(
                                QueryContextDTO.of(codeNorm, baseCodeNorm, deviceNorm, userNorm),
                                errorDto, null,
                                "Formato inválido para 'session'. Esperado: USR\\d{12}",
                                "bad_request"
                        ));
            }
            sessionDto = logQueryService.getSessionByBaseCode(baseCodeNorm); // si no hay eventos, queda null
        }

        return ResponseEntity.ok(new SupportResponseDTO(
                QueryContextDTO.of(codeNorm, baseCodeNorm, deviceNorm, userNorm),
                errorDto,
                sessionDto,
                null,
                "ok"
        ));
    }

    /* ========= Helpers ========= */

    private static String normalizeUpper(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t.toUpperCase();
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
