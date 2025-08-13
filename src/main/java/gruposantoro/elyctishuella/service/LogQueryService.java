package gruposantoro.elyctishuella.service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gruposantoro.elyctishuella.model.ScanLog;
import gruposantoro.elyctishuella.model.dto.ErrorCodeResponseDTO;
import gruposantoro.elyctishuella.model.dto.ScanEventDTO;
import gruposantoro.elyctishuella.model.dto.SessionResponseDTO;
import gruposantoro.elyctishuella.repository.ScanLogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogQueryService {

    private final ScanLogRepository scanLogRepository;

    @Transactional(readOnly = true)
    public ErrorCodeResponseDTO getByErrorCode(String errorCode) {
        List<ScanLog> logs = scanLogRepository.findAllByErrorCodeOrderByDateAsc(errorCode);
        if (logs.isEmpty()) return null;

        List<ScanEventDTO> events = logs.stream().map(this::toDto).toList();

        return ErrorCodeResponseDTO.builder()
                .errorCode(errorCode)
                .count(events.size())
                .firstSeen(events.get(0).getDate())
                .lastSeen(events.get(events.size() - 1).getDate())
                .events(events)
                .build();
    }

    @Transactional(readOnly = true)
    public SessionResponseDTO getSessionByBaseCode(String baseCode) {
        List<ScanLog> logs = scanLogRepository.findAllByBaseCodeOrderByDateAsc(baseCode);
        if (logs.isEmpty()) return null;

        List<ScanEventDTO> events = logs.stream().map(this::toDto).toList();

        int starts = (int) logs.stream().filter(l -> "START".equalsIgnoreCase(l.getType())).count();
        int ends   = (int) logs.stream().filter(l -> "END".equalsIgnoreCase(l.getType())).count();
        int errors = (int) logs.stream().filter(l -> {
            String t = l.getType();
            return t != null && t.equalsIgnoreCase("ERROR");
        }).count();

        boolean completed = (starts > 0 && ends > 0);

        Long durationSeconds = null;
        if (completed) {
            var startAt = events.get(0).getDate();
            var endAt   = events.get(events.size() - 1).getDate();
            if (startAt != null && endAt != null) {
                durationSeconds = Duration.between(startAt, endAt).getSeconds();
            }
        }

        return SessionResponseDTO.builder()
                .baseCode(baseCode)
                .count(events.size())
                .startAt(events.get(0).getDate())
                .endAt(events.get(events.size() - 1).getDate())
                .durationSeconds(durationSeconds)
                .completed(completed)
                .starts(starts)
                .ends(ends)
                .errors(errors)
                .events(events)
                .build();
    }

    private ScanEventDTO toDto(ScanLog l) {
        ScanEventDTO d = new ScanEventDTO();
        d.setId(l.getId());
        d.setDate(l.getDate());
        d.setType(l.getType());
        d.setProcess(l.getProcess());
        d.setMessage(l.getMessage());
        d.setDevice(l.getDevice());
        d.setScanDevice(l.getScanDevice());

        d.setPersonId(l.getPerson() != null ? l.getPerson().getId() : null);

        if (l.getOficina() != null) {
            d.setOficinaId(l.getOficina().getId());
            // Ajusta si el campo en Oficina se llama distinto
            try {
                d.setOficinaNombre(l.getOficina().getNombre());
            } catch (Exception ignore) {
                // si no existe, lo dejamos en null
            }
        }

        d.setErrorCode(l.getErrorCode());
        d.setSessionToken(l.getSessionToken());
        d.setBaseCode(l.getBaseCode());
        d.setTrackingId(l.getTrackingCode() != null ? l.getTrackingCode().getId() : null);
        return d;
    }
}
