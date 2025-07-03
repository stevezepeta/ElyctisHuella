package gruposantoro.elyctishuella.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

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
        Person person = personRepository.findById(request.getPersonId())
            .orElseThrow(() -> new RuntimeException("Persona no encontrada"));

        LocalDateTime scanDateTime;
        try {
            scanDateTime = LocalDateTime.parse(request.getScanDate(), formatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Formato inválido de scanDate. Usa: yyyy-MM-dd HH:mm:ss");
        }

        ScanLog log = new ScanLog();
        log.setDate(scanDateTime);
        log.setType(request.getType());
        log.setPerson(person);
        log.setDevice(request.getDevice());
        log.setScanDevice(request.getScanDevice());
        log.setProcess(request.getProcess());
        log.setMessage(request.getMessage());

        scanLogRepository.save(log);
    }

    public List<ScanLog> getLogsBetweenDates(LocalDateTime from, LocalDateTime to) {
        return scanLogRepository.findAllByDateBetween(from, to);
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
        if (filter.getFromDate() != null) {
            try {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("date"),
                    LocalDateTime.parse(filter.getFromDate(), formatter)
                ));
            } catch (DateTimeParseException e) {
                throw new RuntimeException("Formato inválido en fromDate. Usa: yyyy-MM-dd HH:mm:ss");
            }
        }
        if (filter.getToDate() != null) {
            try {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("date"),
                    LocalDateTime.parse(filter.getToDate(), formatter)
                ));
            } catch (DateTimeParseException e) {
                throw new RuntimeException("Formato inválido en toDate. Usa: yyyy-MM-dd HH:mm:ss");
            }
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        return entityManager.createQuery(query).getResultList();
    }
}
