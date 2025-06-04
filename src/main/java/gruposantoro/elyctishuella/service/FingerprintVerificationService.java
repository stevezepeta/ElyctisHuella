package gruposantoro.elyctishuella.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.machinezoo.sourceafis.FingerprintImage;
import com.machinezoo.sourceafis.FingerprintImageOptions;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;

import gruposantoro.elyctishuella.model.dto.huellas.FingerprintResultDTO;
import gruposantoro.elyctishuella.model.FingerPrint;
import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.repository.FingerPrintRepository;
import gruposantoro.elyctishuella.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FingerprintVerificationService {

    private final PersonRepository personRepository;
    private final FingerPrintRepository fingerPrintRepository;

    private final BigDecimal MAX_SCORE = new BigDecimal("120.00");
    private final BigDecimal MAX_PERCENTAGE = new BigDecimal("100.00");
    private final Base64.Decoder decoder64 = Base64.getDecoder();
    private final BigDecimal MULTIPLIER = MAX_PERCENTAGE.divide(MAX_SCORE, RoundingMode.HALF_DOWN);
    private final BigDecimal THRESHOLD;

    // SOLO este constructor, sin RequiredArgsConstructor
    public FingerprintVerificationService(
            @Value("${guyana.threshold:80}") short threshold,
            PersonRepository personRepository,
            FingerPrintRepository fingerPrintRepository
    ) {
        this.THRESHOLD = BigDecimal.valueOf(threshold);
        this.personRepository = personRepository;
        this.fingerPrintRepository = fingerPrintRepository;
        log.info("Threshold para match de huella: " + threshold);
    }

    public Person verifyByCurpAndFingerprints(String curp, Map<String, MultipartFile> filesFingerprint) throws Exception {
        Optional<Person> personOpt = personRepository.findByCurp(curp);
        if (personOpt.isEmpty()) return null;

        Person person = personOpt.get();

        Optional<FingerPrint> fingerPrintOpt = fingerPrintRepository.findByPerson(person);
        if (fingerPrintOpt.isEmpty()) return null;

        FingerPrint storedFingerPrint = fingerPrintOpt.get();

        // Puedes ajustar qué dedo usar: aquí thumbRight (pulgar derecho)
        String inputThumbRight = fileToBase64(filesFingerprint.get("thumbRight"));
        String storedThumbRight = storedFingerPrint.getThumbRight();

        if (inputThumbRight == null || storedThumbRight == null) return null;

        FingerprintResultDTO result = compareFingerprints(inputThumbRight, storedThumbRight);

        if (result != null && result.isMatch()) {
            return person;
        }
        return null;
    }

    public FingerprintResultDTO compareFingerprints(String fingerprint1Base64, String fingerprint2Base64) {
        try {
            var options = new FingerprintImageOptions().dpi(500);

            byte[] fingerprint1 = decoder64.decode(fingerprint1Base64);
            byte[] fingerprint2 = decoder64.decode(fingerprint2Base64);

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

            FingerprintResultDTO dto = new FingerprintResultDTO();
            dto.setMatch(true);
            dto.setScore(score);
            dto.setPercentage(percentage);
            return dto;
        } catch (Exception e) {
            log.error("Error comparando huellas dactilares: ", e);
            return null;
        }
    }

    private String fileToBase64(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) return null;
        return Base64.getEncoder().encodeToString(file.getBytes());
    }
}
