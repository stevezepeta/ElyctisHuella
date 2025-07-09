package gruposantoro.elyctishuella.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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



}