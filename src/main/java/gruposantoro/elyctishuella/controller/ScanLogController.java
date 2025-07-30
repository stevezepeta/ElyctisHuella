package gruposantoro.elyctishuella.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gruposantoro.elyctishuella.model.ScanLog;
import gruposantoro.elyctishuella.model.dto.ScanLogFilterDTO;
import gruposantoro.elyctishuella.model.dto.ScanLogRequestDTO;
import gruposantoro.elyctishuella.service.ScanLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * End-points públicos para gestionar y consultar los Scan-Logs.
 * <br>Los filtros territoriales se basan en los IDs de país, estado,
 * municipio y oficina (no por nombre).
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class ScanLogController {

    private final ScanLogService scanLogService;

    /* ───────────────────────  CREATE  ─────────────────────── */

   @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> createLog(
            @Valid @RequestBody ScanLogRequestDTO req) {

        Long id = scanLogService.saveLog(req);      // ← ahora compila
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(Map.of("id", id, "status", "saved"));
    }

    /* ───────────────────────  FILTER  ─────────────────────── */

   @GetMapping("/filter")
public ResponseEntity<List<ScanLog>> filterLogs(
        @RequestParam(required = false) Long personId,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String device,
        @RequestParam(required = false) String scanDevice,
        @RequestParam(required = false) String process,
        @RequestParam(required = false) String message,
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate,
        /* IDs territoriales */
        @RequestParam(required = false) Long paisId,
        @RequestParam(required = false) Long estadoId,
        @RequestParam(required = false) Long municipioId,
        @RequestParam(required = false) Long oficinaId   // ← ya no es obligatorio
) {

    ScanLogFilterDTO f = new ScanLogFilterDTO();
    f.setPersonId(personId);
    f.setType(type);
    f.setDevice(device);
    f.setScanDevice(scanDevice);
    f.setProcess(process);
    f.setMessage(message);
    f.setFromDate(fromDate);
    f.setToDate(toDate);

    f.setPaisId(paisId);
    f.setEstadoId(estadoId);
    f.setMunicipioId(municipioId);
    f.setOficinaId(oficinaId);     // null ⇒ el servicio no filtrará por oficina

    return ResponseEntity.ok(scanLogService.searchLogs(f));
}


    /* ───────────────────────  CALENDARIO  ─────────────────────── */

    @GetMapping("/calendar")
    public ResponseEntity<Map<String, List<Integer>>> getFullLogCalendar() {
        return ResponseEntity.ok(scanLogService.getFullCalendarGroupedByMonth());
    }

    /* ───────────────────────  RESUMEN  ─────────────────────── */

   @GetMapping("/summary")
public ResponseEntity<Map<String, Object>> getProcessSummary(
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate,
        @RequestParam(required = false) Long oficinaId) {

    return ResponseEntity.ok(scanLogService.getProcessSummary(fromDate, toDate, oficinaId));
}

}
