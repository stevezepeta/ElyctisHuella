package gruposantoro.elyctishuella.service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.model.ScanLog;
import gruposantoro.elyctishuella.model.dto.ErrorCodeResponseDTO;
import gruposantoro.elyctishuella.model.dto.ScanEventDTO;
import gruposantoro.elyctishuella.model.dto.SessionResponseDTO;
import gruposantoro.elyctishuella.model.dto.SessionUserDTO;
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

        // ===== Usuario de la sesión (tabla person) =====
        Person person = logs.stream()
                .map(ScanLog::getPerson)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        Long userId = (person != null ? person.getId() : null);
        SessionUserDTO user = (person != null ? mapUser(person) : null);

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
                .userId(userId)   // ← agregado
                .user(user)       // ← agregado
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
            try {
                d.setOficinaNombre(l.getOficina().getNombre());
            } catch (Exception ignore) { /* opcional */ }
        }

        d.setErrorCode(l.getErrorCode());
        d.setSessionToken(l.getSessionToken());
        d.setBaseCode(l.getBaseCode());
        d.setTrackingId(l.getTrackingCode() != null ? l.getTrackingCode().getId() : null);
        return d;
    }

    /* ================= Helpers ================= */

    /** Mapea Person → SessionUserDTO con los campos reales de la tabla person. */
    private SessionUserDTO mapUser(Person p) {
        String nombres = safe(p.getNombres());
        String a1 = safe(p.getPrimerApellido());
        String a2 = safe(p.getSegundoApellido());
        String nombreCompleto = (nombres + " " + a1 + " " + a2).trim().replaceAll("\\s+", " ");

        return SessionUserDTO.builder()
                .id(p.getId())
                .curp(p.getCurp())
                .nombres(nombres.isEmpty() ? null : nombres)
                .primerApellido(a1.isEmpty() ? null : a1)
                .segundoApellido(a2.isEmpty() ? null : a2)
                .nombreCompleto(nombreCompleto.isBlank() ? null : nombreCompleto)
                .sexo(p.getSexo())
                .nacionalidad(p.getNacionalidad())
                .fechaNacimiento(p.getFechaNacimiento())
                .direccion(p.getDireccion())
                .build();
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
}
