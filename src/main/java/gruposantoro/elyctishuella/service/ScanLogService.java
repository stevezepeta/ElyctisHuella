package gruposantoro.elyctishuella.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;

import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.model.ScanLog;
import gruposantoro.elyctishuella.model.dto.ScanLogFilterDTO;
import gruposantoro.elyctishuella.model.dto.ScanLogRequestDTO;
import gruposantoro.elyctishuella.repository.PersonRepository;
import gruposantoro.elyctishuella.repository.ScanLogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScanLogService {
    private final ScanLogRepository scanLogRepository;
    private final PersonRepository personRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

 public void saveLog(ScanLogRequestDTO request) {
    LocalDateTime scanDateTime;
    try {
        scanDateTime = LocalDateTime.parse(request.getScanDate(), formatter);
    } catch (DateTimeParseException e) {
        throw new RuntimeException("Formato inválido de scanDate. Usa: yyyy-MM-dd HH:mm:ss");
    }

    ScanLog log = new ScanLog();
    log.setDate(scanDateTime);
    log.setType(request.getType());
    log.setDevice(request.getDevice());
    log.setScanDevice(request.getScanDevice());
    log.setProcess(request.getProcess());
    log.setMessage(request.getMessage());

    // Solo asociar persona si el ID es válido
    if (request.getPersonId() != null && request.getPersonId() != -1) {
        Person person = personRepository.findById(request.getPersonId())
            .orElseThrow(() -> new RuntimeException("Persona no encontrada"));
        log.setPerson(person);
    } else {
        log.setPerson(null); // o simplemente no setear
    }

    scanLogRepository.save(log);
}


  public List<ScanLog> searchLogs(ScanLogFilterDTO filter) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<ScanLog> query = cb.createQuery(ScanLog.class);
    Root<ScanLog> root = query.from(ScanLog.class);

    List<Predicate> predicates = new ArrayList<>();

    if (filter.getPersonId() != null) {
        predicates.add(cb.equal(root.get("person").get("id"), filter.getPersonId()));
    }
    if (filter.getType() != null) {
        predicates.add(cb.equal(root.get("type"), filter.getType()));
    }
    if (filter.getDevice() != null) {
        predicates.add(cb.equal(root.get("device"), filter.getDevice()));
    }
    if (filter.getScanDevice() != null) {
        predicates.add(cb.equal(root.get("scanDevice"), filter.getScanDevice()));
    }
    if (filter.getProcess() != null) {
        predicates.add(cb.equal(root.get("process"), filter.getProcess()));
    }
    if (filter.getMessage() != null) {
        predicates.add(cb.like(root.get("message"), "%" + filter.getMessage() + "%"));
    }

    DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    if (filter.getFromDate() != null) {
        try {
            LocalDate fromDate = LocalDate.parse(filter.getFromDate(), dateOnlyFormatter);

            if (filter.getToDate() == null) {
                // Solo fromDate: rango exacto del día
                predicates.add(cb.between(
                    root.get("date"),
                    fromDate.atStartOfDay(),
                    fromDate.atTime(23, 59, 59, 999_999_999)
                ));
            } else {
                // FromDate + ToDate: aplicar después el toDate
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("date"),
                    fromDate.atStartOfDay()
                ));
            }

        } catch (DateTimeParseException e) {
            throw new RuntimeException("Formato inválido en fromDate. Usa: yyyy-MM-dd");
        }
    }

    if (filter.getToDate() != null) {
        try {
            LocalDate toDate = LocalDate.parse(filter.getToDate(), dateOnlyFormatter);
            predicates.add(cb.lessThanOrEqualTo(
                root.get("date"),
                toDate.atTime(23, 59, 59, 999_999_999)
            ));
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Formato inválido en toDate. Usa: yyyy-MM-dd");
        }
    }

    query.where(cb.and(predicates.toArray(new Predicate[0])));
    return entityManager.createQuery(query).getResultList();
}


public Map<String, List<Integer>> getFullCalendarGroupedByMonth() {
    List<java.sql.Date> sqlDates = scanLogRepository.findAllLogDates();

    Map<String, Set<Integer>> grouped = new HashMap<>();

    for (java.sql.Date sqlDate : sqlDates) {
        LocalDate date = sqlDate.toLocalDate();
        String key = date.getYear() + "-" + String.format("%02d", date.getMonthValue());

        grouped.computeIfAbsent(key, k -> new TreeSet<>()) // TreeSet para orden y no duplicados
               .add(date.getDayOfMonth());
    }

    // Convertir a Map<String, List<Integer>> para el JSON
    Map<String, List<Integer>> result = new HashMap<>();
    for (Map.Entry<String, Set<Integer>> entry : grouped.entrySet()) {
        result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
    }

    return result;
}
public Map<String, Object> getProcessSummary(String fromDate, String toDate) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDateTime from = null;
    LocalDateTime to = null;

    try {
        if (fromDate != null && toDate == null) {
            LocalDate date = LocalDate.parse(fromDate, formatter);
            from = date.atStartOfDay();
            to = date.atTime(23, 59, 59, 999_999_999);
        } else if (fromDate != null && toDate != null) {
            from = LocalDate.parse(fromDate, formatter).atStartOfDay();
            to = LocalDate.parse(toDate, formatter).atTime(23, 59, 59, 999_999_999);
        }
    } catch (DateTimeParseException e) {
        throw new RuntimeException("Formato de fecha inválido. Usa yyyy-MM-dd");
    }

    final LocalDateTime fromFinal = from;
    final LocalDateTime toFinal = to;

    List<ScanLog> allLogs = scanLogRepository.findAll().stream()
        .filter(log -> {
            if (fromFinal != null && toFinal != null) {
                return !log.getDate().isBefore(fromFinal) && !log.getDate().isAfter(toFinal);
            } else if (fromFinal != null) {
                return !log.getDate().isBefore(fromFinal);
            }
            return true;
        })
        .sorted(Comparator.comparing(ScanLog::getDate)) // Muy importante para orden cronológico
        .toList();

    List<ScanLog> startLogs = allLogs.stream()
        .filter(log -> "START".equalsIgnoreCase(log.getType()))
        .toList();

    List<ScanLog> endLogs = allLogs.stream()
        .filter(log -> "END".equalsIgnoreCase(log.getType()))
        .toList();

    List<Map<String, Object>> matchedLogs = new ArrayList<>();
    Map<String, Integer> countByProcess = new HashMap<>();
    Map<String, List<Long>> durationsByProcess = new HashMap<>();

    // Se usarán logs de START disponibles
    List<ScanLog> remainingStarts = new ArrayList<>(startLogs);

    for (ScanLog end : endLogs) {
        Optional<ScanLog> matchingStart = remainingStarts.stream()
            .filter(start ->
                start.getPerson() != null &&
                end.getPerson() != null &&
                start.getPerson().getId().equals(end.getPerson().getId()) &&
                Objects.equals(start.getDevice(), end.getDevice()) &&
                Objects.equals(start.getProcess(), end.getProcess()) &&
                start.getDate().isBefore(end.getDate())
            )
            .max(Comparator.comparing(ScanLog::getDate)); // Elegir el último START antes del END

        if (matchingStart.isPresent()) {
            ScanLog start = matchingStart.get();

            long seconds = Duration.between(start.getDate(), end.getDate()).getSeconds();
            durationsByProcess.computeIfAbsent(start.getProcess(), k -> new ArrayList<>()).add(seconds);
            countByProcess.merge(start.getProcess(), 1, Integer::sum);

            Map<String, Object> match = new HashMap<>();
           match.put("person", start.getPerson()); // 👈 objeto completo
            match.put("device", start.getDevice());
            match.put("process", start.getProcess());
            match.put("startDate", start.getDate());
            match.put("endDate", end.getDate());
            match.put("durationSeconds", seconds);
            matchedLogs.add(match);

            remainingStarts.remove(start); // Evitar reuso de mismo START
        }
    }

    Map<String, Double> averageDurations = new HashMap<>();
    durationsByProcess.forEach((proc, list) -> {
        double avg = list.stream().mapToLong(Long::longValue).average().orElse(0);
        averageDurations.put(proc, avg);
    });

    Map<String, Object> result = new HashMap<>();
    result.put("completedCount", countByProcess);
    result.put("averageDurationSeconds", averageDurations);
    result.put("matchedLogs", matchedLogs);
    return result;
}


}