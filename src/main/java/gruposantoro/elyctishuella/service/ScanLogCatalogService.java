package gruposantoro.elyctishuella.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.model.ScanLog;
import gruposantoro.elyctishuella.repository.ScanLogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScanLogCatalogService {

    private final ScanLogRepository scanLogRepository;

    public List<Person> getUniquePersons() {
        return scanLogRepository.findAll().stream()
            .map(ScanLog::getPerson)
            .filter(p -> p != null)
            .distinct()
            .collect(Collectors.toList());
    }

    public List<String> getUniqueDevices() {
        return scanLogRepository.findAll().stream()
            .map(ScanLog::getDevice)
            .filter(d -> d != null && !d.isBlank())
            .distinct()
            .collect(Collectors.toList());
    }

    public List<String> getUniqueScanDevices() {
        return scanLogRepository.findAll().stream()
            .map(ScanLog::getScanDevice)
            .filter(sd -> sd != null && !sd.isBlank())
            .distinct()
            .collect(Collectors.toList());
    }
}
