package gruposantoro.elyctishuella.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.model.dto.EnrollPersonDTO;
import gruposantoro.elyctishuella.model.dto.huellas.EnrollBiometricDataDTO;
import gruposantoro.elyctishuella.model.dto.huellas.PersonEnrolledDTO;
import gruposantoro.elyctishuella.repository.PersonRepository;
import gruposantoro.elyctishuella.rulesException.EnrollException;
import gruposantoro.elyctishuella.rulesException.ModelNotFoundException;
import gruposantoro.elyctishuella.service.EnrollCustomerService;
import gruposantoro.elyctishuella.service.FingerprintService;
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
 private final FingerprintService fingerprintService;
    private final PersonRepository personRepository;
  @PostMapping("/enroll/biographic")
public ResponseEntity<Message> enrollBiographic(@RequestBody EnrollPersonDTO dto) {

    /* ①  Persistir datos biográficos (ya no lanza EnrollException) */
    Person personSaved = enrollCustomerService.enrollBiographic(dto);

    /* ②  Nombre completo */
    String nombreCompleto = Stream.of(
            personSaved.getNombres(),
            personSaved.getPrimerApellido(),
            personSaved.getSegundoApellido())
        .filter(Objects::nonNull)
        .collect(Collectors.joining(" "));

    /* ③  Payload de respuesta */
    PersonEnrolledDTO payload = PersonEnrolledDTO.builder()
            .idPerson(personSaved.getId())
            .nombreCompleto(nombreCompleto)
            .oficinaId(
                personSaved.getOficina() != null
                    ? personSaved.getOficina().getId()
                    : null
            )
            .build();

    /* ④  OK */
    return ResponseEntity.ok(
            new Message(true,
                        "Datos biográficos enrolados correctamente",
                        payload));
}



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
