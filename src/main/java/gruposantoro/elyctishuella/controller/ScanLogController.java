package gruposantoro.elyctishuella.controller;

import java.util.List;

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
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class ScanLogController {

    private final ScanLogService scanLogService;

    @PostMapping
    public ResponseEntity<Void> createLog(@RequestBody ScanLogRequestDTO request) {
        scanLogService.saveLog(request);
        return ResponseEntity.ok().build();
    }
 @GetMapping("/filter")
public ResponseEntity<List<ScanLog>> filterLogs(
        @RequestParam(required = false) Long personId,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String device,
        @RequestParam(required = false) String scanDevice,
        @RequestParam(required = false) String process,
        @RequestParam(required = false) String message,
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate
) {
    ScanLogFilterDTO filter = new ScanLogFilterDTO();
    filter.setPersonId(personId);
    filter.setType(type);
    filter.setDevice(device);
    filter.setScanDevice(scanDevice);
    filter.setProcess(process);
    filter.setMessage(message);
    filter.setFromDate(fromDate);
    filter.setToDate(toDate);

    List<ScanLog> result = scanLogService.searchLogs(filter);
    return ResponseEntity.ok(result);
}


}
 