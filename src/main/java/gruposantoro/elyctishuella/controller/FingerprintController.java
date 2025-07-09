package gruposantoro.elyctishuella.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

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

    private final EnrollCustomerService enrollCustomerService;
    private final PersonRepository personRepository;
    private final FingerPrintRepository fingerPrintRepository;
    private final FingerprintService fingerprintService;

    @PostMapping("/verify")
    public ResponseEntity<FingerprintVerificationResponseDTO> verifyFingerprint(
            @RequestParam("curp") String curp,
            @RequestParam Map<String, MultipartFile> filesBiometric,
            @RequestParam(value = "facePhoto", required = false) MultipartFile facePhoto
    ) {
        log.info("=== [POST /api/fingerprint/verify] ===");
        log.info("CURP: {}", curp);
        log.info("filesBiometric keys: {}", filesBiometric.keySet());
        log.info("facePhoto: {}", (facePhoto != null) ? facePhoto.getOriginalFilename() : "null");

        try {
            Person person = personRepository.findByCurp(curp).orElse(null);
            if (person == null) {
                log.warn("Persona NO encontrada para CURP: {}", curp);
                return ResponseEntity.badRequest().build();
            }
            log.info("Persona encontrada: {} {}", person.getNombres(), person.getPrimerApellido());

            FingerPrint fingerPrint = fingerPrintRepository.findByPerson(person).orElse(null);
            if (fingerPrint == null) {
                log.warn("Huella NO encontrada para persona con CURP: {}", curp);
                return ResponseEntity.notFound().build();
            }
            log.info("Registro de huella encontrado para persona: {}", curp);

            String[] fingerKeys = {
                "thumbLeft", "indexLeft", "middleLeft", "ringLeft", "littleLeft",
                "thumbRight", "indexRight", "middleRight", "ringRight", "littleRight"
            };

            boolean atLeastOneFingerPresent = false;
            boolean matchFound = false;
            String matchedFinger = null;
            FingerprintResultDTO matchResult = null;

            for (String finger : fingerKeys) {
                MultipartFile file = filesBiometric.get(finger);
                String savedFingerprintPath = getFingerprintPath(fingerPrint, finger);
                log.info("[{}] Archivo recibido: {}", finger, file != null ? file.getOriginalFilename() : "null");
                log.info("[{}] Path guardado en BD: {}", finger, savedFingerprintPath);

                if (file != null && !file.isEmpty() && savedFingerprintPath != null) {
                    atLeastOneFingerPresent = true;
                    log.info("[{}] Peso archivo subido: {} bytes", finger, file.getSize());

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

            String nombreCompleto = person.getNombres();
            if (person.getPrimerApellido() != null) {
                nombreCompleto += " " + person.getPrimerApellido();
            }
            if (person.getSegundoApellido() != null) {
                nombreCompleto += " " + person.getSegundoApellido();
            }
            nombreCompleto = nombreCompleto.trim();

            FingerprintVerificationResponseDTO response = new FingerprintVerificationResponseDTO();

            if (!atLeastOneFingerPresent) {
                log.warn("No se recibió ninguna huella digital para verificar.");
                response.setMatch(false);
                response.setNombreCompleto("");
                response.setId(null);
                return ResponseEntity.ok(response);
            }

            if (!matchFound) {
                log.warn("NO hubo match entre huellas para CURP {} en ninguno de los 10 dedos", curp);
                response.setMatch(false);
                response.setNombreCompleto("");
                response.setId(null);
                return ResponseEntity.ok(response);
            }

            log.info("¡MATCH exitoso! Dedo: {} - Score: {}, Porcentaje: {}",
                    matchedFinger, matchResult.getScore(), matchResult.getPercentage());

            response.setMatch(true);
            response.setNombreCompleto(nombreCompleto);
            response.setId(person.getId());

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Error comparando huellas dactilares: ", ex);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String getFingerprintPath(FingerPrint fingerPrint, String finger) {
        switch (finger) {
            case "thumbLeft": return fingerPrint.getThumbLeft();
            case "indexLeft": return fingerPrint.getIndexLeft();
            case "middleLeft": return fingerPrint.getMiddleLeft();
            case "ringLeft": return fingerPrint.getRingLeft();
            case "littleLeft": return fingerPrint.getLittleLeft();
            case "thumbRight": return fingerPrint.getThumbRight();
            case "indexRight": return fingerPrint.getIndexRight();
            case "middleRight": return fingerPrint.getMiddleRight();
            case "ringRight": return fingerPrint.getRingRight();
            case "littleRight": return fingerPrint.getLittleRight();
            default: return null;
        }
    }
}
