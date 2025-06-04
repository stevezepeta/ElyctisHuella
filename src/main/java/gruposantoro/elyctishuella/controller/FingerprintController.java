package gruposantoro.elyctishuella.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import gruposantoro.elyctishuella.rulesException.ModelNotFoundException;
import gruposantoro.elyctishuella.service.EnrollCustomerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fingerprint")
public class FingerprintController {

    private final EnrollCustomerService enrollCustomerService;

    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyFingerprint(
            @RequestParam String curp,
            @RequestParam Map<String, MultipartFile> filesBiometric
    ) throws IOException, ModelNotFoundException {
        boolean isMatch = enrollCustomerService.verifyBiometric(curp, filesBiometric);
        return ResponseEntity.ok(isMatch);
    }
}
