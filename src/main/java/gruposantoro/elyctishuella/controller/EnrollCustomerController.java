package gruposantoro.elyctishuella.controller;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.model.dto.EnrollPersonDTO;
import gruposantoro.elyctishuella.model.dto.huellas.EnrollBiometricDataDTO;
import gruposantoro.elyctishuella.model.dto.huellas.PersonEnrolledDTO;
import gruposantoro.elyctishuella.rulesException.EnrollException;
import gruposantoro.elyctishuella.rulesException.ModelNotFoundException;
import gruposantoro.elyctishuella.service.EnrollCustomerService;
import gruposantoro.elyctishuella.util.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/enrollCustomer")
public class EnrollCustomerController {

    private final ObjectMapper objectMapper;
    private final EnrollCustomerService enrollCustomerService;

    /**
     * REGISTRO BIOGRÁFICO (JSON)
     * ✅ Incluye "password" en el DTO (texto plano en request).
     * 🔒 El service debe HASHEAR (BCrypt) y guardar el hash (por ejemplo en person.password).
     *
     * Postman:
     * POST /api/enrollCustomer/enroll/biographic
     * Headers: Content-Type: application/json
     * Body raw JSON incluye "password"
     */
    @PostMapping("/enroll/biographic")
    public ResponseEntity<Message> enrollBiographic(@RequestBody EnrollPersonDTO dto) {

        // ① Persistir datos biográficos (incluye password)
        Person personSaved = enrollCustomerService.enrollBiographic(dto);

        // ② Construir nombre completo
        String nombreCompleto = Stream.of(
                        personSaved.getNombres(),
                        personSaved.getPrimerApellido(),
                        personSaved.getSegundoApellido())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "))
                .trim();

        // ③ Payload de respuesta
        PersonEnrolledDTO payload = PersonEnrolledDTO.builder()
                .idPerson(personSaved.getId())
                .nombreCompleto(nombreCompleto)
                .build();

        // ④ OK
        return ResponseEntity.ok(
                new Message(true,
                        "Datos biográficos enrolados correctamente",
                        payload));
    }

    /**
     * REGISTRO BIOMÉTRICO (multipart/form-data)
     * - filesBiometric: archivos de huellas (thumbLeft, indexLeft, etc.)
     * - info: JSON en string con info extra (ej: curp, personId, etc.)
     */
    @PostMapping("/enroll/fingerprint")
    public ResponseEntity<Message> enrollBiometric(
            @RequestParam Map<String, MultipartFile> filesBiometric,
            @RequestParam("info") @NotNull @NotBlank String info
    ) throws IOException, EnrollException, ModelNotFoundException {

        EnrollBiometricDataDTO enrollCustomerDataDTO =
                objectMapper.readValue(info, EnrollBiometricDataDTO.class);

        enrollCustomerService.enrollBiometric(enrollCustomerDataDTO, filesBiometric);

        return ResponseEntity.ok(
                new Message(true, "Biometric data enrolled successfully", null)
        );
    }
}
