package gruposantoro.elyctishuella.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import gruposantoro.elyctishuella.model.TrackingCode;
import gruposantoro.elyctishuella.repository.TrackingCodeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrackingCodeService {

    private final TrackingCodeRepository trackingCodeRepository;

    public TrackingCode generateNewTrackingCode() {
        String codigo = generateSixDigitCode();

        TrackingCode code = new TrackingCode();
        code.setCodigo(codigo);
        return trackingCodeRepository.save(code);
    }

    private String generateSixDigitCode() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000); // asegura 6 dígitos
        return String.valueOf(number);
    }

    public TrackingCode getById(Long id) {
        return trackingCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TrackingCode no encontrado con ID: " + id));
    }
}
