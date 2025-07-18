package gruposantoro.elyctishuella.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.service.ScanLogCatalogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class ScanLogCatalogController {

    private final ScanLogCatalogService scanLogCatalogService;

    // 1. Usuarios únicos desde scan_log
    @GetMapping("/persons")
    public ResponseEntity<List<Person>> getAllPersonsFromLogs() {
        return ResponseEntity.ok(scanLogCatalogService.getUniquePersons());
    }

    // 2. Dispositivos únicos desde scan_log
    @GetMapping("/devices")
    public ResponseEntity<List<String>> getUniqueDevices() {
        return ResponseEntity.ok(scanLogCatalogService.getUniqueDevices());
    }

    // 3. ScanDevices únicos desde scan_log
    @GetMapping("/scan-devices")
    public ResponseEntity<List<String>> getUniqueScanDevices() {
        return ResponseEntity.ok(scanLogCatalogService.getUniqueScanDevices());
    }
}
