package gruposantoro.elyctishuella.controller;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gruposantoro.elyctishuella.model.Oficina;
import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.model.dto.ErrorCodeResponseDTO;
import gruposantoro.elyctishuella.model.dto.ScanEventDTO;
import gruposantoro.elyctishuella.model.dto.SessionResponseDTO;
import gruposantoro.elyctishuella.model.dto.SessionUserDTO;
import gruposantoro.elyctishuella.model.dto.support.SupportRequestDTO;
import gruposantoro.elyctishuella.repository.OficinaRepository;
import gruposantoro.elyctishuella.repository.PersonRepository;
import gruposantoro.elyctishuella.service.LogQueryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SupportJsonController {

    private final LogQueryService   logQueryService;
    private final PersonRepository  personRepository;
    private final OficinaRepository oficinaRepository;

    // USR + 9–15 dígitos o prefijo alfanumérico (6+), y sufijo 3–5 letras + 3 dígitos o END
    private static final Pattern ERROR_CODE_RX = Pattern.compile(
        "^(?:USR\\d{9,15}|[A-Z0-9_-]{6,})-(?:[A-Z]{3,5}\\d{3}|END)$"
    );
    private static final Pattern BASE_CODE_RX  = Pattern.compile("^USR\\d{9,15}$");

    /* ───────── 2.1: /api/error-codes/{code} → UN SOLO ARREGLO de items unificados ───────── */
    @GetMapping(value = {"/api/error-codes/{code}", "/error-codes/{code}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String,Object>>> getErrorUnified(@PathVariable("code") String rawCode) {
        String code = normalizeUpper(rawCode);
        if (code == null || !ERROR_CODE_RX.matcher(code).matches()) return ResponseEntity.ok(List.of());

        ErrorCodeResponseDTO dto = logQueryService.getByErrorCode(code);
        List<ScanEventDTO> events = (dto != null && dto.getEvents() != null) ? dto.getEvents() : List.of();
        List<Map<String,Object>> out = new ArrayList<>(events.size());

        // cache simple para no consultar repetido
        Map<Long, SessionUserDTO> personCache = new HashMap<>();
        Map<Long, Oficina> officeCache = new HashMap<>();

        for (ScanEventDTO e : events) {
            out.add(toUnifiedItem(e, personCache, officeCache));
        }
        return ResponseEntity.ok(out);
    }

    /* ───────── 2.2: /api/sessions/{baseCode} → UN SOLO ARREGLO de items unificados ───────── */
    @GetMapping(value = {"/api/sessions/{baseCode}", "/sessions/{baseCode}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String,Object>>> getSessionUnified(@PathVariable("baseCode") String rawBase) {
        String base = normalizeUpper(rawBase);
        if (base == null || !BASE_CODE_RX.matcher(base).matches()) return ResponseEntity.ok(List.of());

        SessionResponseDTO dto = logQueryService.getSessionByBaseCode(base);
        List<ScanEventDTO> events = (dto != null && dto.getEvents() != null) ? dto.getEvents() : List.of();

        Map<Long, SessionUserDTO> personCache = new HashMap<>();
        Map<Long, Oficina> officeCache = new HashMap<>();
        List<Map<String,Object>> out = new ArrayList<>(events.size());
        for (ScanEventDTO e : events) {
            out.add(toUnifiedItem(e, personCache, officeCache));
        }
        return ResponseEntity.ok(out);
    }

    /* ───────── 3.x: /api/support (GET/POST) → UN SOLO ARREGLO de items unificados ───────── */
    @GetMapping(value = {"/api/support", "/support"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String,Object>>> supportGet(
            @RequestParam(name = "code") String code,
            @RequestParam(name = "session", required = false) String baseCode,
            @RequestParam(name = "device",  required = false) String device,
            @RequestParam(name = "user",    required = false) String userId
    ) {
        return ResponseEntity.ok(buildSupportUnified(code, baseCode));
    }

    @PostMapping(value = {"/api/support", "/support"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String,Object>>> supportPost(@RequestBody(required = false) SupportRequestDTO req) {
        String code     = req != null ? req.getCode()    : null;
        String baseCode = req != null ? req.getSession() : null;
        return ResponseEntity.ok(buildSupportUnified(code, baseCode));
    }

    private List<Map<String,Object>> buildSupportUnified(String code, String baseCode) {
        final String codeNorm     = normalizeUpper(code);
        final String baseCodeNorm = normalizeUpper(baseCode);

        if (codeNorm == null || !ERROR_CODE_RX.matcher(codeNorm).matches()) return List.of();

        ErrorCodeResponseDTO errorDto = logQueryService.getByErrorCode(codeNorm);
        SessionResponseDTO sessionDto = null;
        if (baseCodeNorm != null && BASE_CODE_RX.matcher(baseCodeNorm).matches()) {
            sessionDto = logQueryService.getSessionByBaseCode(baseCodeNorm);
        }

        List<ScanEventDTO> merged = mergeEvents(
                errorDto != null ? errorDto.getEvents() : null,
                sessionDto != null ? sessionDto.getEvents() : null
        );

        Map<Long, SessionUserDTO> personCache = new HashMap<>();
        Map<Long, Oficina> officeCache = new HashMap<>();
        List<Map<String,Object>> out = new ArrayList<>(merged.size());
        for (ScanEventDTO e : merged) {
            out.add(toUnifiedItem(e, personCache, officeCache));
        }
        return out;
    }

    /* ============================== Helpers ============================== */

    /** Convierte un ScanEventDTO a un item unificado con person + oficina. */
    private Map<String,Object> toUnifiedItem(ScanEventDTO e,
                                             Map<Long, SessionUserDTO> personCache,
                                             Map<Long, Oficina> officeCache) {
        Map<String,Object> item = new LinkedHashMap<>();

        // Campos base del evento
        item.put("id",           e.getId());
        item.put("date",         e.getDate());
        item.put("type",         e.getType());
        item.put("device",       e.getDevice());
        item.put("scanDevice",   e.getScanDevice());
        item.put("process",      e.getProcess());
        item.put("message",      e.getMessage());
        item.put("errorCode",    e.getErrorCode());
        item.put("sessionToken", e.getSessionToken());
        item.put("baseCode",     e.getBaseCode());
        item.put("trackingCode", e.getTrackingId()); // nombre según tu base de ejemplo

        // PERSON embebida
        SessionUserDTO person = null;
        Long personId = e.getPersonId();
        if (personId != null) {
            person = personCache.get(personId);
            if (person == null) {
                Person p = personRepository.findById(personId).orElse(null);
                if (p != null) {
                    person = mapPerson(p);
                    personCache.put(personId, person);
                }
            }
        }
        item.put("person", person);

        // OFICINA embebida (desde BD si es posible)
        Map<String,Object> oficinaObj = null;
        Long oficinaId = e.getOficinaId();
        if (oficinaId != null) {
            Oficina ofi = officeCache.get(oficinaId);
            if (ofi == null) {
                ofi = oficinaRepository.findById(oficinaId).orElse(null);
                if (ofi != null) officeCache.put(oficinaId, ofi);
            }
            if (ofi != null) {
                oficinaObj = new LinkedHashMap<>();
                oficinaObj.put("id", ofi.getId());
                oficinaObj.put("nombre", safe(ofi.getNombre()));
                oficinaObj.put("direccion", getDireccionSafe(ofi)); // por si existe el campo en tu entidad
                oficinaObj.put("paisId", ofi.getPaisId());
                oficinaObj.put("estadoId", ofi.getEstadoId());
                oficinaObj.put("municipioId", ofi.getMunicipioId());

                // Copia denormalizada al nivel del item (como en tu base)
                item.put("paisId", ofi.getPaisId());
                item.put("estadoId", ofi.getEstadoId());
                item.put("municipioId", ofi.getMunicipioId());
            } else {
                // Fallback si no existe en BD: usa lo que venga del evento
                oficinaObj = new LinkedHashMap<>();
                oficinaObj.put("id", oficinaId);
                oficinaObj.put("nombre", e.getOficinaNombre());
                oficinaObj.put("direccion", null);
                oficinaObj.put("paisId", null);
                oficinaObj.put("estadoId", null);
                oficinaObj.put("municipioId", null);

                // Denormalizados nulos si no tenemos datos
                item.put("paisId", null);
                item.put("estadoId", null);
                item.put("municipioId", null);
            }
        }
        item.put("oficina", oficinaObj);

        return item;
    }

    /** Intenta obtener getDireccion() vía reflexión si existe en la entidad Oficina. */
    private String getDireccionSafe(Oficina o) {
        try {
            Method m = o.getClass().getMethod("getDireccion");
            Object val = m.invoke(o);
            return val != null ? val.toString() : null;
        } catch (Exception ignore) {
            return null;
        }
    }

    private List<ScanEventDTO> mergeEvents(List<ScanEventDTO> a, List<ScanEventDTO> b) {
        Map<String, ScanEventDTO> map = new LinkedHashMap<>();
        if (a != null) for (ScanEventDTO e : a) map.put(dedupKey(e), e);
        if (b != null) for (ScanEventDTO e : b) map.putIfAbsent(dedupKey(e), e);
        List<ScanEventDTO> list = new ArrayList<>(map.values());
        list.sort(Comparator.comparing(ScanEventDTO::getDate, Comparator.nullsLast(LocalDateTime::compareTo)));
        return list;
    }

    private String dedupKey(ScanEventDTO e) {
        if (e.getId() != null) return "ID:" + e.getId();
        String d = (e.getDate() != null ? e.getDate().toString() : "null");
        String t = String.valueOf(e.getType());
        String p = String.valueOf(e.getProcess());
        String m = String.valueOf(e.getMessage());
        return String.join("|", "K", d, t, p, m);
    }

    private static String normalizeUpper(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t.toUpperCase();
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private SessionUserDTO mapPerson(Person p) {
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
