package gruposantoro.elyctishuella.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import gruposantoro.elyctishuella.model.Oficina;
import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.model.ScanLog;
import gruposantoro.elyctishuella.model.dto.ScanLogFilterDTO;
import gruposantoro.elyctishuella.model.dto.ScanLogRequestDTO;
import gruposantoro.elyctishuella.repository.OficinaRepository;
import gruposantoro.elyctishuella.repository.PersonRepository;
import gruposantoro.elyctishuella.repository.ScanLogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Servicio central para creación y consulta de ScanLog.
 */
@Service
@RequiredArgsConstructor
public class ScanLogService {

    /* ──────────────── Repositorios ─────────────── */
    private final ScanLogRepository scanLogRepository;
    private final PersonRepository  personRepository;
    private final OficinaRepository oficinaRepository;

    /* ──────────────── JPA Criteria ─────────────── */
    @PersistenceContext
    private EntityManager entityManager;

    /* ──────────────── Formatos de fecha ─────────── */
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /* ════════════════════════ CREATE ════════════════════════ */

    /** Persiste un log y devuelve el ID generado. */
   @Transactional
public Long saveLog(ScanLogRequestDTO req) {

    /* 1️⃣  Fecha/hora del evento */
    LocalDateTime scanDate = parseDateTime(req.getScanDate());

    /* 2️⃣  Oficina  (–1 ⇒ “sin oficina”) */
    Oficina oficina = null;
    if (req.getOficinaId() == null) {
        throw new RuntimeException("oficinaId es obligatorio (use –1 si no aplica)");
    }
    if (req.getOficinaId() > 0) {                       // sólo valida si es > 0
        oficina = oficinaRepository.findById(req.getOficinaId())
                .orElseThrow(() ->
                        new RuntimeException("Oficina con id %d no existe"
                                             .formatted(req.getOficinaId())));
    }
    // cuando es –1, oficina queda null

    /* 3️⃣  Construcción del ScanLog */
    ScanLog log = new ScanLog();
    log.setDate(scanDate);
    log.setType(req.getType());
    log.setDevice(req.getDevice());
    log.setScanDevice(req.getScanDevice());
    log.setProcess(req.getProcess());
    log.setMessage(req.getMessage());

    // ---- Oficina y campos territoriales solo si existe ----
    if (oficina != null) {
        log.setOficina(oficina);
        log.setPaisId(oficina.getPaisId());
        log.setEstadoId(oficina.getEstadoId());
        log.setMunicipioId(oficina.getMunicipioId());
    }

    /* 4️⃣  Persona (opcional) */
    if (req.getPersonId() != null && req.getPersonId() > 0) {
        Person p = personRepository.findById(req.getPersonId())
                                   .orElseThrow(() ->
                                           new RuntimeException("Persona con id %d no existe"
                                                                .formatted(req.getPersonId())));
        log.setPerson(p);
    }

    /* 5️⃣  Persistir y devolver id */
    return scanLogRepository.save(log).getId();
}

    /* ════════════════════════ FILTER ════════════════════════ */

    public List<ScanLog> searchLogs(ScanLogFilterDTO f) {

        CriteriaBuilder cb  = entityManager.getCriteriaBuilder();
        CriteriaQuery<ScanLog> cq = cb.createQuery(ScanLog.class);
        Root<ScanLog> root = cq.from(ScanLog.class);

        List<Predicate> predicates = new ArrayList<>();

        // Filtros básicos
        if (f.getPersonId()   != null) predicates.add(cb.equal(root.get("person").get("id"), f.getPersonId()));
        if (f.getType()       != null) predicates.add(cb.equal(root.get("type"),       f.getType()));
        if (f.getDevice()     != null) predicates.add(cb.equal(root.get("device"),     f.getDevice()));
        if (f.getScanDevice() != null) predicates.add(cb.equal(root.get("scanDevice"), f.getScanDevice()));
        if (f.getProcess()    != null) predicates.add(cb.equal(root.get("process"),    f.getProcess()));
        if (f.getMessage()    != null) predicates.add(cb.like(root.get("message"), "%" + f.getMessage() + "%"));

        // Territoriales
        if (f.getPaisId()      != null) predicates.add(cb.equal(root.get("paisId"),      f.getPaisId()));
        if (f.getEstadoId()    != null) predicates.add(cb.equal(root.get("estadoId"),    f.getEstadoId()));
        if (f.getMunicipioId() != null) predicates.add(cb.equal(root.get("municipioId"), f.getMunicipioId()));
        if (f.getOficinaId()   != null) predicates.add(cb.equal(root.get("oficina").get("id"), f.getOficinaId()));

        // Rango de fechas (inclusivo)
        if (f.getFromDate() != null) {
            LocalDate d = parseDateOnly(f.getFromDate());
            predicates.add(cb.greaterThanOrEqualTo(root.get("date"), d.atStartOfDay()));
        }
        if (f.getToDate() != null) {
            LocalDate d = parseDateOnly(f.getToDate());
            predicates.add(cb.lessThanOrEqualTo(root.get("date"), d.atTime(23, 59, 59, 999_999_999)));
        }

        cq.where(predicates.toArray(Predicate[]::new));
        return entityManager.createQuery(cq).getResultList();
    }

    /* ═══════════════════════ CALENDARIO ═══════════════════════ */

    public Map<String,List<Integer>> getFullCalendarGroupedByMonth() {

        List<java.sql.Date> raw = scanLogRepository.findAllLogDates();

        return raw.stream()
                  .map(java.sql.Date::toLocalDate)
                  .collect(Collectors.groupingBy(
                          d -> d.getYear() + "-" + String.format("%02d", d.getMonthValue()),
                          LinkedHashMap::new,
                          Collectors.mapping(LocalDate::getDayOfMonth,
                                             Collectors.collectingAndThen(Collectors.toCollection(TreeSet::new),
                                                                          ArrayList::new))));
    }

    /* ═══════════════════════ SUMMARY ═══════════════════════ */
    /* ═══════════════════════ SUMMARY ═══════════════════════ */
public Map<String, Object> getProcessSummary(String fromDate, String toDate, Long oficinaId) {
    LocalDateTime from = null;
    LocalDateTime to = null;

    if (fromDate != null && (toDate == null || fromDate.equals(toDate))) {
        // Solo un día exacto
        LocalDate d = parseDateOnly(fromDate);
        from = d.atStartOfDay();
        to = d.atTime(23, 59, 59, 999_999_999);
    } else {
        if (fromDate != null) from = parseDateOnly(fromDate).atStartOfDay();
        if (toDate != null)   to   = parseDateOnly(toDate).atTime(23, 59, 59, 999_999_999);
    }

    // Construir consulta base con CriteriaBuilder
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<ScanLog> cq = cb.createQuery(ScanLog.class);
    Root<ScanLog> root = cq.from(ScanLog.class);

    List<Predicate> predicates = new ArrayList<>();
    if (from != null)       predicates.add(cb.greaterThanOrEqualTo(root.get("date"), from));
    if (to != null)         predicates.add(cb.lessThanOrEqualTo(root.get("date"), to));
    if (oficinaId != null)  predicates.add(cb.equal(root.get("oficina").get("id"), oficinaId));

    cq.where(predicates.toArray(Predicate[]::new));
    cq.orderBy(cb.asc(root.get("date")));

    // Ejecutar consulta
    List<ScanLog> all = entityManager.createQuery(cq).getResultList();

    // Separar logs en START y END
    List<ScanLog> starts = all.stream()
        .filter(l -> "START".equalsIgnoreCase(l.getType()))
        .toList();
    List<ScanLog> ends = all.stream()
        .filter(l -> "END".equalsIgnoreCase(l.getType()))
        .toList();

    // Estructuras para resultado
    Map<String, Integer> completed = new HashMap<>();
    Map<String, List<Long>> durs = new HashMap<>();
    List<Map<String, Object>> pairs = new ArrayList<>();
    List<ScanLog> open = new ArrayList<>(starts);

    for (ScanLog end : ends) {
        open.stream()
            .filter(st -> samePair(st, end) && st.getDate().isBefore(end.getDate()))
            .max(Comparator.comparing(ScanLog::getDate))
            .ifPresent(st -> {
                long secs = Duration.between(st.getDate(), end.getDate()).getSeconds();
                durs.computeIfAbsent(st.getProcess(), k -> new ArrayList<>()).add(secs);
                completed.merge(st.getProcess(), 1, Integer::sum);

                Map<String, Object> m = new LinkedHashMap<>();
                m.put("person",          st.getPerson());
                m.put("device",          st.getDevice());
                m.put("process",         st.getProcess());
                m.put("startDate",       st.getDate());
                m.put("endDate",         end.getDate());
                m.put("durationSeconds", secs);
                m.put("oficinaId",       st.getOficina() != null ? st.getOficina().getId() : null);
                pairs.add(m);

                open.remove(st);
            });
    }

    Map<String, Double> avg = new HashMap<>();
    durs.forEach((k, v) -> avg.put(k, v.stream().mapToLong(Long::longValue).average().orElse(0)));

    return Map.of(
        "completedCount",         completed,
        "averageDurationSeconds", avg,
        "matchedLogs",            pairs
    );
}




    /* ═══════════════════════ HELPERS ═══════════════════════ */

    private LocalDateTime parseDateTime(String s) {
        try { return LocalDateTime.parse(s, DATE_TIME_FMT); }
        catch (DateTimeParseException ex) {
            throw new RuntimeException("scanDate inválido. Usa yyyy-MM-dd HH:mm:ss");
        }
    }

    private LocalDate parseDateOnly(String s) {
        try { return LocalDate.parse(s, DATE_ONLY_FMT); }
        catch (DateTimeParseException ex) {
            throw new RuntimeException("Fecha inválida. Usa yyyy-MM-dd");
        }
    }

    private boolean inRange(LocalDateTime d, LocalDateTime from, LocalDateTime to) {
        return (from == null || !d.isBefore(from)) &&
               (to   == null || !d.isAfter(to));
    }

    /** Misma persona + dispositivo + proceso → pareja START/END. */
    private boolean samePair(ScanLog a, ScanLog b) {
        return Objects.equals(a.getPerson(),  b.getPerson()) &&
               Objects.equals(a.getDevice(),  b.getDevice()) &&
               Objects.equals(a.getProcess(), b.getProcess());
    }
}
