package gruposantoro.elyctishuella.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.machinezoo.sourceafis.FingerprintImage;
import com.machinezoo.sourceafis.FingerprintImageOptions;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;

import gruposantoro.elyctishuella.model.FingerPrint;
import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.model.dto.huellas.FingerprintDataDTO;
import gruposantoro.elyctishuella.model.dto.huellas.FingerprintResultDTO;
import gruposantoro.elyctishuella.repository.FingerPrintRepository;
import gruposantoro.elyctishuella.repository.PersonRepository;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class FingerprintService {

    private final FingerPrintRepository fingerPrintRepository;
    private final PersonRepository personRepository;
    private final String facePhotoDirectory;

    private final BigDecimal THRESHOLD;
    private final BigDecimal MAX_SCORE = new BigDecimal("120.00");
    private final BigDecimal MAX_PERCENTAGE = new BigDecimal("100.00");
    private final BigDecimal MULTIPLIER = MAX_PERCENTAGE.divide(MAX_SCORE, RoundingMode.HALF_DOWN);

    private final Base64.Decoder decoder64 = Base64.getDecoder();

    // Constructor con @Value para inyectar propiedades
    public FingerprintService(
            @Value("${guyana.threshold:80}") short threshold,
            @Value("${face.photo.directory}") String facePhotoDirectory,
            PersonRepository personRepository,
            FingerPrintRepository fingerPrintRepository
    ) {
        this.THRESHOLD = BigDecimal.valueOf(threshold);
        this.personRepository = personRepository;
        this.fingerPrintRepository = fingerPrintRepository;
        this.facePhotoDirectory = facePhotoDirectory;
        log.info("Threshold set to: " + threshold);
        log.info("Ruta para guardar fotos de rostro: {}", facePhotoDirectory);
    }

    @Transactional
    public void saveFingerprintData(FingerprintDataDTO dto, Map<String, MultipartFile> filesFingerprint) throws IOException {
        Person person = personRepository.findByCurp(dto.getCurp())
                .orElseThrow(() -> new IllegalArgumentException("Person not found for CURP: " + dto.getCurp()));
        // Aquí iría la lógica para guardar las huellas y sus imágenes en la BD
        // (asocia huellas al person y guarda imágenes usando filesFingerprint)
    }

    private String fileToBase64(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        return Base64.getEncoder().encodeToString(file.getBytes());
    }

    // Convierte una ruta de archivo a base64
    private String filePathToBase64(String filePath) {
        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (Exception e) {
            log.error("No se pudo leer la imagen desde la ruta: " + filePath, e);
            return null;
        }
    }

    /**
     * Compara dos huellas en base64 (o una como ruta) y devuelve el DTO resultado.
     * Si no hacen match, retorna null.
     */
    public FingerprintResultDTO compareFingerprints(String fingerprint1Base64, String fingerprint2PathOrBase64, boolean isStoredPath) {
        String fingerprint2Base64 = fingerprint2PathOrBase64;
        if (isStoredPath) {
            fingerprint2Base64 = filePathToBase64(fingerprint2PathOrBase64);
            if (fingerprint2Base64 == null) {
                log.warn("No se pudo convertir la ruta a base64: {}", fingerprint2PathOrBase64);
                return null;
            }
        }

        byte[] fingerprint1 = decoder64.decode(fingerprint1Base64);
        byte[] fingerprint2 = decoder64.decode(fingerprint2Base64);

        var options = new FingerprintImageOptions().dpi(500);

        var image1 = new FingerprintImage(fingerprint1, options);
        var template1 = new FingerprintTemplate(image1);

        var image2 = new FingerprintImage(fingerprint2, options);
        var template2 = new FingerprintTemplate(image2);

        FingerprintMatcher matcher = new FingerprintMatcher(template1);
        BigDecimal score = BigDecimal.valueOf(matcher.match(template2));

        boolean isMatch = score.compareTo(THRESHOLD) > 0;

        BigDecimal scoreAux = score.compareTo(MAX_SCORE) > 0 ? MAX_SCORE : score;
        BigDecimal percentage = scoreAux.multiply(MULTIPLIER).setScale(2, RoundingMode.HALF_DOWN);

        if (!isMatch) {
            return null;
        }

        // Retorna DTO con resultados
        FingerprintResultDTO dto = new FingerprintResultDTO();
        dto.setMatch(true);
        dto.setScore(score);
        dto.setPercentage(percentage);
        return dto;
    }

    // ==== MÉTODO PARA VERIFICAR MATCH DE HUELLAS ====
    public Person verifyBiometric(String curp, Map<String, MultipartFile> filesBiometric) throws IOException {
        Optional<Person> personOpt = personRepository.findByCurp(curp);
        if (personOpt.isEmpty()) return null;
        Person person = personOpt.get();

        Optional<FingerPrint> fingerPrintOpt = fingerPrintRepository.findByPerson(person);
        if (fingerPrintOpt.isEmpty()) return null;
        FingerPrint fingerprints = fingerPrintOpt.get();

        String[] fingerKeys = {
            "thumbLeft", "indexLeft", "middleLeft", "ringLeft", "littleLeft",
            "thumbRight", "indexRight", "middleRight", "ringRight", "littleRight"
        };

        for (String finger : fingerKeys) {
            MultipartFile file = filesBiometric.get(finger);
            String storedFingerprintPath = getFingerprintPath(fingerprints, finger);

            if (file != null && !file.isEmpty() && storedFingerprintPath != null) {
                String uploadedBase64 = fileToBase64(file);
                String storedBase64 = filePathToBase64(storedFingerprintPath);
                if (uploadedBase64 != null && storedBase64 != null) {
                    FingerprintResultDTO result = compareFingerprints(uploadedBase64, storedBase64, false);
                    if (result != null && result.isMatch()) {
                        return person; // Retorna la persona que hizo match
                    }
                }
            }
        }
        return null; // Si no hubo coincidencias
    }

    // Utilitario: obtiene la ruta del archivo guardado para cada dedo
    private String getFingerprintPath(FingerPrint fingerprints, String finger) {
        switch (finger) {
            case "thumbLeft": return fingerprints.getThumbLeft();
            case "indexLeft": return fingerprints.getIndexLeft();
            case "middleLeft": return fingerprints.getMiddleLeft();
            case "ringLeft": return fingerprints.getRingLeft();
            case "littleLeft": return fingerprints.getLittleLeft();
            case "thumbRight": return fingerprints.getThumbRight();
            case "indexRight": return fingerprints.getIndexRight();
            case "middleRight": return fingerprints.getMiddleRight();
            case "ringRight": return fingerprints.getRingRight();
            case "littleRight": return fingerprints.getLittleRight();
            default: return null;
        }
    }

    // Guarda o actualiza la foto de rostro y retorna la ruta
    public String saveOrUpdateFacePhoto(Person person, MultipartFile facePhoto) {
        try {
            if (facePhoto != null && !facePhoto.isEmpty()) {
                File dir = new File(facePhotoDirectory);
                if (!dir.exists()) dir.mkdirs();
                String filename = person.getCurp() + "_face_" + System.currentTimeMillis() + ".jpg";
                String path = facePhotoDirectory + File.separator + filename;

                try (FileOutputStream fos = new FileOutputStream(path)) {
                    fos.write(facePhoto.getBytes());
                }
                log.info("Foto facial guardada/actualizada en: {}", path);
                return path;
            }
        } catch (Exception e) {
            log.error("No se pudo guardar la foto facial para la persona: {}", person.getCurp(), e);
        }
        return null;
    }
}
