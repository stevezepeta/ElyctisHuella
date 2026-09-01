package gruposantoro.elyctishuella.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gruposantoro.elyctishuella.model.FingerPrint;
import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.model.dto.FingerprintVerificationResponseDTO;
import gruposantoro.elyctishuella.model.dto.huellas.FingerprintResultDTO;
import gruposantoro.elyctishuella.repository.FingerPrintRepository;
import gruposantoro.elyctishuella.repository.PersonRepository;
import gruposantoro.elyctishuella.service.EnrollCustomerService;
import gruposantoro.elyctishuella.service.FingerprintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fingerprint")
public class FingerprintController {

    private final EnrollCustomerService enrollCustomerService; // lo dejas si lo usas en otro endpoint
    private final PersonRepository personRepository;
    private final FingerPrintRepository fingerPrintRepository;
    private final FingerprintService fingerprintService;

    /**
     * /verify híbrido:
     * - Si llegan huellas: compara biometría
     * - Si NO llegan huellas: permite password (opcional) como fallback
     *
     * Postman (form-data):
     * curp: Text
     * password: Text (opcional)
     * thumbLeft/indexLeft/...: File (opcional)
     * facePhoto: File (opcional)
     */
    @PostMapping("/verify")
    public ResponseEntity<FingerprintVerificationResponseDTO> verifyFingerprint(
            @RequestParam("curp") String curp,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam Map<String, MultipartFile> filesBiometric,
            @RequestParam(value = "facePhoto", required = false) MultipartFile facePhoto
    ) {
        log.info("=== [POST /api/fingerprint/verify] ===");
        log.info("CURP: {}", curp);
        log.info("filesBiometric keys: {}", filesBiometric.keySet());
        log.info("facePhoto: {}", (facePhoto != null) ? facePhoto.getOriginalFilename() : "null");
        log.info("password enviado: {}", (password != null && !password.isBlank()) ? "SI" : "NO");

        FingerprintVerificationResponseDTO response = new FingerprintVerificationResponseDTO();

        try {
            Person person = personRepository.findByCurp(curp).orElse(null);
            if (person == null) {
                log.warn("Persona NO encontrada para CURP: {}", curp);
                return ResponseEntity.badRequest().build();
            }

            String nombreCompleto = buildNombreCompleto(person);

            // 1) Detectar si llegó al menos una huella
            boolean anyFinger = fingerprintService.hasAnyFingerprint(filesBiometric);

            // =========================================================
            // A) SI LLEGARON HUELLAS -> Validar biometría (ignorar password)
            // =========================================================
            if (anyFinger) {

                FingerPrint fingerPrint = fingerPrintRepository.findByPerson(person).orElse(null);
                if (fingerPrint == null) {
                    log.warn("Huella NO encontrada para persona con CURP: {}", curp);
                    return ResponseEntity.notFound().build();
                }

                String[] fingerKeys = {
                        "thumbLeft", "indexLeft", "middleLeft", "ringLeft", "littleLeft",
                        "thumbRight", "indexRight", "middleRight", "ringRight", "littleRight"
                };

                boolean matchFound = false;
                String matchedFinger = null;
                FingerprintResultDTO matchResult = null;

                for (String finger : fingerKeys) {
                    MultipartFile file = filesBiometric.get(finger);
                    String savedFingerprintPath = getFingerprintPath(fingerPrint, finger);

                    log.info("[{}] Archivo recibido: {}", finger, file != null ? file.getOriginalFilename() : "null");
                    log.info("[{}] Path guardado en BD: {}", finger, savedFingerprintPath);

                    if (file != null && !file.isEmpty() && savedFingerprintPath != null) {
                        byte[] uploadedFingerprintBytes = file.getBytes();
                        byte[] savedFingerprintBytes = Files.readAllBytes(Paths.get(savedFingerprintPath));

                        FingerprintResultDTO result = fingerprintService.compareFingerprints(
                                uploadedFingerprintBytes, savedFingerprintBytes
                        );

                        if (result != null) {
                            log.info("[{}] Resultado comparación: match={}, score={}, porcentaje={}",
                                    finger, result.isMatch(), result.getScore(), result.getPercentage());
                        } else {
                            log.info("[{}] Resultado comparación: null (NO match)", finger);
                        }

                        if (result != null && result.isMatch()) {
                            matchFound = true;
                            matchedFinger = finger;
                            matchResult = result;
                            break;
                        }
                    }
                }

                if (!matchFound) {
                    log.warn("NO hubo match entre huellas para CURP {} en ninguno de los 10 dedos", curp);

                    response.setMatch(false);
                    response.setNombreCompleto("");
                    response.setId(null);
                    response.setOficinaId(null);

                    response.setStatus("NO_MATCH");
                    response.setAllowPasswordFallback(false);
                    return ResponseEntity.status(401).body(response);
                }

                log.info("¡MATCH exitoso! Dedo: {} - Score: {}, Porcentaje: {}",
                        matchedFinger, matchResult.getScore(), matchResult.getPercentage());

                response.setMatch(true);
                response.setNombreCompleto(nombreCompleto);
                response.setId(person.getId());
                response.setOficinaId(null);

                response.setStatus("MATCH");
                response.setAllowPasswordFallback(false);
                return ResponseEntity.ok(response);
            }

            // =========================================================
            // B) NO LLEGARON HUELLAS -> Permitir password fallback (TEXTO PLANO)
            // =========================================================
            if (password == null || password.isBlank()) {
                log.warn("No se recibió ninguna huella y NO se envió password.");

                response.setMatch(false);
                response.setNombreCompleto("");
                response.setId(null);
                response.setOficinaId(null);

                response.setStatus("NO_FINGERPRINTS");
                response.setAllowPasswordFallback(true);
                return ResponseEntity.ok(response);
            }

            // ✅ Validar password en TEXTO PLANO (sin encriptación)
            if (!validatePasswordPlain(person, password)) {
                log.warn("Password incorrecto para CURP: {}", curp);

                response.setMatch(false);
                response.setNombreCompleto("");
                response.setId(null);
                response.setOficinaId(null);

                response.setStatus("BAD_PASSWORD");
                response.setAllowPasswordFallback(true);
                return ResponseEntity.status(401).body(response);
            }

            // ✅ Password OK -> devolver como MATCH (igual que biometría)
            response.setMatch(true);
            response.setNombreCompleto(nombreCompleto);
            response.setId(person.getId());
            response.setOficinaId(null);

            response.setStatus("MATCH");
            response.setAllowPasswordFallback(false);
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Error en verify híbrido: ", ex);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ================= Helpers =================

    /**
     * ✅ Password en TEXTO PLANO (sin BCrypt)
     * Person debe tener campo String password (getter getPassword()).
     */
    private boolean validatePasswordPlain(Person person, String rawPassword) {
        String stored = person.getPassword();
        if (stored == null) return false;
        return stored.equals(rawPassword);
    }

    private String buildNombreCompleto(Person person) {
        return Stream.of(person.getNombres(), person.getPrimerApellido(), person.getSegundoApellido())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "))
                .trim();
    }

    private String getFingerprintPath(FingerPrint fingerPrint, String finger) {
        return switch (finger) {
            case "thumbLeft" -> fingerPrint.getThumbLeft();
            case "indexLeft" -> fingerPrint.getIndexLeft();
            case "middleLeft" -> fingerPrint.getMiddleLeft();
            case "ringLeft" -> fingerPrint.getRingLeft();
            case "littleLeft" -> fingerPrint.getLittleLeft();
            case "thumbRight" -> fingerPrint.getThumbRight();
            case "indexRight" -> fingerPrint.getIndexRight();
            case "middleRight" -> fingerPrint.getMiddleRight();
            case "ringRight" -> fingerPrint.getRingRight();
            case "littleRight" -> fingerPrint.getLittleRight();
            default -> null;
        };
    }
}
