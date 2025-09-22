package gruposantoro.elyctishuella.domain;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gruposantoro.elyctishuella.model.ScanLog;
import gruposantoro.elyctishuella.model.dto.LogDTO;
import gruposantoro.elyctishuella.model.dto.OficinaDTO;
import gruposantoro.elyctishuella.model.dto.PersonDTO;
import gruposantoro.elyctishuella.model.dto.PersonSummaryDTO;
import gruposantoro.elyctishuella.model.dto.TokenSessionResponse;
import gruposantoro.elyctishuella.repository.ScanLogRepository;

@Service
public class TokenSessionService {

    private final ScanLogRepository repository;

    public TokenSessionService(ScanLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public TokenSessionResponse getLogsBySessionToken(String sessionToken) {
        List<ScanLog> logs = repository.findBySessionTokenOrderByDateAsc(sessionToken);
        List<LogDTO> data = logs.stream().map(this::toLogDTO).toList();

        Map<String, List<ScanLog>> grouped = logs.stream()
                .collect(Collectors.groupingBy(this::personKey));

        List<PersonSummaryDTO> persons = grouped.values().stream()
                .map(list -> {
                    ScanLog any = list.get(0);
                    return new PersonSummaryDTO(
                            nz(getCurp(any)),
                            nz(getNombres(any)),
                            nz(getPrimerApellido(any)),
                            nz(getSegundoApellido(any)),
                            list.size()
                    );
                })
                .sorted(Comparator.comparing(PersonSummaryDTO::curp, Comparator.nullsLast(String::compareTo)))
                .toList();

        TokenSessionResponse.Meta meta =
                new TokenSessionResponse.Meta(sessionToken, data.size(), persons.size());

        return new TokenSessionResponse(data, persons, meta);
    }

    /* -------------------- Mapeo a LogDTO -------------------- */

    private LogDTO toLogDTO(ScanLog e) {
        PersonDTO person = null;
        if (hasAnyPersonField(e)) {
            person = new PersonDTO();
            person.setCurp(n(getCurp(e)));
            person.setNombres(n(getNombres(e)));
            person.setPrimerApellido(n(getPrimerApellido(e)));
            person.setSegundoApellido(n(getSegundoApellido(e)));
            person.setSexo(n(getSexo(e)));
            person.setNacionalidad(n(getNacionalidad(e)));
            person.setFechaNacimiento(getFechaNacimiento(e));
            person.setDireccion(n(getDireccion(e)));
            // 🔧 Antes: person.setOficinaId(e.getOficinaId());  // ❌ no existe en Person
            person.setOficinaId(getOficinaRelationId(e));        // ✅ usa el id de la Oficina relacionada (si existe)
            // Si tu Oficina no tiene id, usa: person.setOficinaId(null);
        }

        OficinaDTO oficina = null;
        if (hasAnyOficinaField(e)) {
            oficina = new OficinaDTO();
            oficina.setNombre(n(getOficinaNombre(e)));
            oficina.setDireccion(n(getOficinaDireccion(e)));
            oficina.setPaisId(getOficinaPaisId(e));
            oficina.setEstadoId(getOficinaEstadoId(e));
            oficina.setMunicipioId(getOficinaMunicipioId(e));
        }

        OffsetDateTime date = toOffset(e.getDate()); // LocalDateTime → OffsetDateTime (UTC)
        return new LogDTO(
                e.getId(),
                date,
                e.getType(),
                e.getProcess(),
                e.getMessage(),
                e.getErrorCode(),
                e.getSessionToken(),
                e.getBaseCode(),
                person,
                oficina
        );
    }

    /* -------------------- Helpers de acceso seguro -------------------- */

    // Person
    private String getCurp(ScanLog e)            { return e.getPerson()  != null ? e.getPerson().getCurp()            : null; }
    private String getNombres(ScanLog e)         { return e.getPerson()  != null ? e.getPerson().getNombres()         : null; }
    private String getPrimerApellido(ScanLog e)  { return e.getPerson()  != null ? e.getPerson().getPrimerApellido()  : null; }
    private String getSegundoApellido(ScanLog e) { return e.getPerson()  != null ? e.getPerson().getSegundoApellido() : null; }
    private String getSexo(ScanLog e)            { return e.getPerson()  != null ? e.getPerson().getSexo()            : null; }
    private String getNacionalidad(ScanLog e)    { return e.getPerson()  != null ? e.getPerson().getNacionalidad()    : null; }
    private java.time.LocalDate getFechaNacimiento(ScanLog e) { return e.getPerson() != null ? e.getPerson().getFechaNacimiento() : null; }
    private String getDireccion(ScanLog e)       { return e.getPerson()  != null ? e.getPerson().getDireccion()       : null; }

    // Oficina (relación)
    private Long   getOficinaRelationId(ScanLog e){ return e.getOficina() != null ? e.getOficina().getId()            : null; } // ← usa el id de Oficina
    private String getOficinaNombre(ScanLog e)    { return e.getOficina() != null ? e.getOficina().getNombre()        : null; }
    private String getOficinaDireccion(ScanLog e) { return e.getOficina() != null ? e.getOficina().getDireccion()     : null; }
    private Long   getOficinaPaisId(ScanLog e)    { return e.getOficina() != null ? e.getOficina().getPaisId()        : null; }
    private Long   getOficinaEstadoId(ScanLog e)  { return e.getOficina() != null ? e.getOficina().getEstadoId()      : null; }
    private Long   getOficinaMunicipioId(ScanLog e){ return e.getOficina()!= null ? e.getOficina().getMunicipioId()   : null; }

    private OffsetDateTime toOffset(LocalDateTime ldt) {
        return ldt == null ? null : ldt.atOffset(ZoneOffset.UTC);
    }

    /* -------------------- Agrupación persons -------------------- */

    private String personKey(ScanLog e) {
        String curp = getCurp(e);
        if (notBlank(curp)) return curp.trim();

        return String.join("|",
                s(getNombres(e)),
                s(getPrimerApellido(e)),
                s(getSegundoApellido(e))
        ).trim();
    }

    private boolean hasAnyPersonField(ScanLog e) {
        return notBlank(getCurp(e)) ||
               notBlank(getNombres(e)) ||
               notBlank(getPrimerApellido(e)) ||
               notBlank(getSegundoApellido(e)) ||
               notBlank(getSexo(e)) ||
               notBlank(getNacionalidad(e)) ||
               getFechaNacimiento(e) != null ||
               notBlank(getDireccion(e)) ||
               getOficinaRelationId(e) != null; // ← actualizado
    }

    private boolean hasAnyOficinaField(ScanLog e) {
        return notBlank(getOficinaNombre(e)) ||
               notBlank(getOficinaDireccion(e)) ||
               getOficinaPaisId(e) != null ||
               getOficinaEstadoId(e) != null ||
               getOficinaMunicipioId(e) != null;
    }

    /* -------------------- Utils -------------------- */

    private boolean notBlank(String s) { return s != null && !s.isBlank(); }
    private String n(String s)         { return (s == null || s.isBlank()) ? null : s.trim(); }
    private String nz(String s)        { return (s == null || s.isBlank()) ? null : s.trim(); }
    private String s(String v)         { return (v == null) ? "" : v.trim(); }
}
