package gruposantoro.elyctishuella.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import gruposantoro.elyctishuella.model.dto.huellas.EnrollBiometricDataDTO;
import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.model.dto.EnrollPersonDTO;
import gruposantoro.elyctishuella.model.dto.huellas.PersonEnrolledDTO;
import gruposantoro.elyctishuella.rulesException.EnrollException;
import gruposantoro.elyctishuella.rulesException.ModelNotFoundException;
import gruposantoro.elyctishuella.service.EnrollCustomerService;
import gruposantoro.elyctishuella.util.Message;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/enrollCustomer")
public class EnrollCustomerController {

    private final ObjectMapper objectMapper;
    private final EnrollCustomerService enrollCustomerService;

    @PostMapping("/enroll/biographic")
    public ResponseEntity<Message> enrollBiographic(@RequestBody @Valid EnrollPersonDTO enrollPersonDTO) {
        Person personSaved = enrollCustomerService.enrollBiographic(enrollPersonDTO);

        // ¡Ojo! Si tienes separados "primerApellido" y "segundoApellido", concaténalos:
        String nombreCompleto = personSaved.getNombres();
        if (personSaved.getPrimerApellido() != null) {
            nombreCompleto += " " + personSaved.getPrimerApellido();
        }
        if (personSaved.getSegundoApellido() != null) {
            nombreCompleto += " " + personSaved.getSegundoApellido();
        }

        PersonEnrolledDTO personEnrolled = PersonEnrolledDTO.builder()
            .idPerson(personSaved.getId())
            .nombreCompleto(nombreCompleto.trim())
            .build();

        return ResponseEntity.ok(
            new Message(true, "Datos biográficos enrolados correctamente", personEnrolled)
        );
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
