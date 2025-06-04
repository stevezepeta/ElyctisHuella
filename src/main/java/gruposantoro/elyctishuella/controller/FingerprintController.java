package gruposantoro.elyctishuella.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.model.dto.FingerprintVerificationResponseDTO;
import gruposantoro.elyctishuella.repository.PersonRepository;
import gruposantoro.elyctishuella.rulesException.ModelNotFoundException;
import gruposantoro.elyctishuella.service.EnrollCustomerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fingerprint")
public class FingerprintController {

    private final EnrollCustomerService enrollCustomerService;
    private final PersonRepository personRepository; // <--- Agrega esto

    @PostMapping("/verify")
    public ResponseEntity<FingerprintVerificationResponseDTO> verifyFingerprint(
            @RequestParam String curp,
            @RequestParam Map<String, MultipartFile> filesBiometric
    ) throws IOException, ModelNotFoundException {
        boolean isMatch = enrollCustomerService.verifyBiometric(curp, filesBiometric);

        String nombreCompleto = "";
        if (isMatch) {
            Optional<Person> personOpt = personRepository.findByCurp(curp);
            if (personOpt.isPresent()) {
                Person person = personOpt.get();
                nombreCompleto = (person.getNombres() + " " +
                        (person.getPrimerApellido() != null ? person.getPrimerApellido() + " " : "") +
                        (person.getSegundoApellido() != null ? person.getSegundoApellido() : "")).trim();
            }
        }

        FingerprintVerificationResponseDTO response = new FingerprintVerificationResponseDTO(isMatch, nombreCompleto);
        return ResponseEntity.ok(response);
    }
}
