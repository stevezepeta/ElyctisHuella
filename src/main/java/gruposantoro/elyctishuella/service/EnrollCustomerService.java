package gruposantoro.elyctishuella.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import gruposantoro.elyctishuella.model.FingerPrint;
import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.model.dto.EnrollPersonDTO;
import gruposantoro.elyctishuella.model.dto.huellas.EnrollBiometricDataDTO;
import gruposantoro.elyctishuella.repository.FingerPrintRepository;
import gruposantoro.elyctishuella.repository.PersonRepository;
import gruposantoro.elyctishuella.rulesException.EnrollException;
import gruposantoro.elyctishuella.rulesException.ModelNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnrollCustomerService {

    private final PersonRepository personRepository;
    private final FingerPrintRepository fingerPrintRepository;
    private final ImageService imageService;

    // === ALTA BIOGRÁFICA ===
    @Transactional
    public Person enrollBiographic(EnrollPersonDTO dto) {

        // 1️⃣ Construir persona sin oficina
        Person person = new Person();
        person.setCurp(dto.getCurp());
        person.setNombres(dto.getNombres());
        person.setPrimerApellido(dto.getPrimerApellido());
        person.setSegundoApellido(dto.getSegundoApellido());
        person.setSexo(dto.getSexo());
        person.setNacionalidad(dto.getNacionalidad());
        person.setFechaNacimiento(dto.getFechaNacimiento());
        person.setDireccion(dto.getDireccion());
        // explícitamente sin oficina

        Person saved = personRepository.save(person);

        log.info("Persona enrolada: {} {} (sin oficina)",
                 saved.getNombres(),
                 saved.getPrimerApellido());

        return saved;
    }

    // === ENROLAMIENTO BIOMÉTRICO con DTO ===
    @Transactional
    public void enrollBiometric(
            EnrollBiometricDataDTO enrollBiometricDataDTO,
            Map<String, MultipartFile> filesBiometric
    ) throws EnrollException, ModelNotFoundException, IOException {

        Long idPerson = Long.valueOf(enrollBiometricDataDTO.getIdPerson());

        // Buscar persona por ID
        Person person = personRepository.findById(idPerson)
                .orElseThrow(() -> new ModelNotFoundException(Person.class, idPerson));

        String[] fingerKeys = {
            "thumbLeft", "indexLeft", "middleLeft", "ringLeft", "littleLeft",
            "thumbRight", "indexRight", "middleRight", "ringRight", "littleRight"
        };

        // Mapeo de estatus de cada dedo
        Map<String, FingerPrint.fingerStatus> fingerStatuses = new HashMap<>();
        fingerStatuses.put("indexLeft", enrollBiometricDataDTO.getIndexLeftStatus());
        fingerStatuses.put("middleLeft", enrollBiometricDataDTO.getMiddleLeftStatus());
        fingerStatuses.put("ringLeft", enrollBiometricDataDTO.getRingLeftStatus());
        fingerStatuses.put("littleLeft", enrollBiometricDataDTO.getLittleLeftStatus());
        fingerStatuses.put("thumbLeft", enrollBiometricDataDTO.getThumbLeftStatus());
        fingerStatuses.put("thumbRight", enrollBiometricDataDTO.getThumbRightStatus());
        fingerStatuses.put("indexRight", enrollBiometricDataDTO.getIndexRightStatus());
        fingerStatuses.put("middleRight", enrollBiometricDataDTO.getMiddleRightStatus());
        fingerStatuses.put("ringRight", enrollBiometricDataDTO.getRingRightStatus());
        fingerStatuses.put("littleRight", enrollBiometricDataDTO.getLittleRightStatus());

        // Validar que al menos una huella fue enviada
        boolean atLeastOneFingerPresent = false;
        for (String finger : fingerKeys) {
            MultipartFile file = filesBiometric.get(finger);
            if (file != null && !file.isEmpty()) {
                atLeastOneFingerPresent = true;
                break;
            }
        }
        if (!atLeastOneFingerPresent) {
            throw new EnrollException("Debe enviar al menos una huella digital.");
        }

        // Guardar imágenes y estatus
        Map<String, String> filesImages = new HashMap<>();
        for (String finger : fingerKeys) {
            MultipartFile file = filesBiometric.get(finger);
            FingerPrint.fingerStatus status = fingerStatuses.get(finger);
            if (file == null || (status != null && (
                    status == FingerPrint.fingerStatus.A ||
                    status == FingerPrint.fingerStatus.N ||
                    status == FingerPrint.fingerStatus.B))) {
                filesImages.put(finger, null);
            } else {
                String imageUrl = imageService.saveImage(file, finger, String.valueOf(person.getId()), ImageService.ImageType.CUSTOMER);
                filesImages.put(finger, imageUrl);
            }
        }

        // Crear y guardar la entidad FingerPrint
        FingerPrint userFingerPrint = FingerPrint.builder()
            .thumbLeft(filesImages.get("thumbLeft")).thumbLeftStatus(fingerStatuses.get("thumbLeft"))
            .indexLeft(filesImages.get("indexLeft")).indexLeftStatus(fingerStatuses.get("indexLeft"))
            .middleLeft(filesImages.get("middleLeft")).middleLeftStatus(fingerStatuses.get("middleLeft"))
            .ringLeft(filesImages.get("ringLeft")).ringLeftStatus(fingerStatuses.get("ringLeft"))
            .littleLeft(filesImages.get("littleLeft")).littleLeftStatus(fingerStatuses.get("littleLeft"))
            .thumbRight(filesImages.get("thumbRight")).thumbRightStatus(fingerStatuses.get("thumbRight"))
            .indexRight(filesImages.get("indexRight")).indexRightStatus(fingerStatuses.get("indexRight"))
            .middleRight(filesImages.get("middleRight")).middleRightStatus(fingerStatuses.get("middleRight"))
            .ringRight(filesImages.get("ringRight")).ringRightStatus(fingerStatuses.get("ringRight"))
            .littleRight(filesImages.get("littleRight")).littleRightStatus(fingerStatuses.get("littleRight"))
            .status("ENROLLED")
            .date(LocalDateTime.now())
            .person(person)
            .build();

        fingerPrintRepository.save(userFingerPrint);

        log.info("Enrollment fingerprints registered successfully for {} {}",
                person.getNombres(), person.getPrimerApellido());
    }

    // ==== VERIFICACIÓN DE HUELLAS ====
    public boolean verifyBiometric(String curp, Map<String, MultipartFile> filesBiometric) throws IOException, ModelNotFoundException {
        Person person = personRepository.findByCurp(curp)
                .orElseThrow(() -> new ModelNotFoundException(Person.class, curp));

        Optional<FingerPrint> fingerPrintOpt = fingerPrintRepository.findByPerson(person);
        if (fingerPrintOpt.isEmpty()) {
            throw new ModelNotFoundException(FingerPrint.class, person.getId());
        }
        FingerPrint fingerprints = fingerPrintOpt.get();

        String[] fingerKeys = {
            "thumbLeft", "indexLeft", "middleLeft", "ringLeft", "littleLeft",
            "thumbRight", "indexRight", "middleRight", "ringRight", "littleRight"
        };

        for (String finger : fingerKeys) {
            MultipartFile file = filesBiometric.get(finger);
            if (file != null && !file.isEmpty()) {
                String storedFingerprintPath = getFingerprintPath(fingerprints, finger);
                if (storedFingerprintPath != null) {
                    byte[] storedBytes = imageService.loadImageBytes(storedFingerprintPath);
                    byte[] uploadedBytes = file.getBytes();
                    if (storedBytes != null && uploadedBytes != null && java.util.Arrays.equals(storedBytes, uploadedBytes)) {
                        return true; // Coincidencia encontrada
                    }
                }
            }
        }
        return false;
    }

    // Utilitario: obtiene la ruta del archivo guardado para cada dedo
    private String getFingerprintPath(FingerPrint fingerprints, String finger) {
        return switch (finger) {
            case "thumbLeft" -> fingerprints.getThumbLeft();
            case "indexLeft" -> fingerprints.getIndexLeft();
            case "middleLeft" -> fingerprints.getMiddleLeft();
            case "ringLeft" -> fingerprints.getRingLeft();
            case "littleLeft" -> fingerprints.getLittleLeft();
            case "thumbRight" -> fingerprints.getThumbRight();
            case "indexRight" -> fingerprints.getIndexRight();
            case "middleRight" -> fingerprints.getMiddleRight();
            case "ringRight" -> fingerprints.getRingRight();
            case "littleRight" -> fingerprints.getLittleRight();
            default -> null;
        };
    }
}
