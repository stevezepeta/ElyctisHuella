package gruposantoro.elyctishuella.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gruposantoro.elyctishuella.model.TrackingCode;
import gruposantoro.elyctishuella.service.TrackingCodeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingCodeController {

    private final TrackingCodeService trackingCodeService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> generateTrackingCode() {
        TrackingCode code = trackingCodeService.generateNewTrackingCode();

        return ResponseEntity.ok(Map.of(
            "id", code.getId(),
            "codigo", code.getCodigo()
        ));
    }
}

