package gruposantoro.elyctishuella.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import gruposantoro.elyctishuella.model.Oficina;
import gruposantoro.elyctishuella.model.dto.OficinaDTO;
import gruposantoro.elyctishuella.model.dto.OficinaFilterDTO;
import gruposantoro.elyctishuella.model.dto.StateDTO;
import gruposantoro.elyctishuella.repository.OficinaRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

/**
 * Servicio para creación y consulta de oficinas.
 */
@Service
@RequiredArgsConstructor
public class OficinaService {

    private final OficinaRepository oficinaRepository;
    private final CatalogService    catalogService;   // servicio de catálogos (país / estado / ciudad)

 /* ═════════════════════ CREAR OFICINA ═════════════════════ */
public Oficina crear(OficinaDTO dto) {

    /* 1️⃣ Validaciones mínimas */
    if (dto.getPaisId() == null || dto.getEstadoId() == null || dto.getMunicipioId() == null) {
        throw new RuntimeException("paisId, estadoId y municipioId son obligatorios");
    }

    /* 2️⃣ País existe */
    boolean paisOk = catalogService.getAllCountries().stream()   // SimpleDTO (record)
            .anyMatch(p -> Objects.equals(p.id(), dto.getPaisId()));
    if (!paisOk) {
        throw new RuntimeException("País no encontrado");
    }

    /* 3️⃣ Estado pertenece al país */
    List<StateDTO> estados = catalogService.getStatesByCountryId(dto.getPaisId());
    StateDTO estado = estados.stream()
            .filter(e -> Objects.equals(e.getId(), dto.getEstadoId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("El estado no pertenece al país seleccionado"));

    /* 4️⃣ Municipio pertenece al estado */
    boolean municipioOk = estado.getCities().stream()            // List<CityDTO>
            .anyMatch(c -> Objects.equals(c.getId(), dto.getMunicipioId()));
    if (!municipioOk) {
        throw new RuntimeException("El municipio no pertenece al estado seleccionado");
    }

    /* 5️⃣ Persistencia */
    Oficina oficina = new Oficina();
    oficina.setNombre(dto.getNombre());
    oficina.setDireccion(dto.getDireccion());
    oficina.setPaisId(dto.getPaisId());
    oficina.setEstadoId(dto.getEstadoId());
    oficina.setMunicipioId(dto.getMunicipioId());

    return oficinaRepository.save(oficina);
}


    /* ════════════════════ BUSCAR ════════════════════ */

    /**
     * Busca oficinas filtrando opcionalmente por país, estado y municipio.<br>
     * <pre>
     * - Si se envía sólo paisId → devuelve todas las oficinas del país.
     * - Si se envía paisId + estadoId → oficinas del estado.
     * - Si además se envía municipioId → oficinas del municipio.
     * </pre>
     */
    public List<Oficina> buscar(OficinaFilterDTO f) {

        Specification<Oficina> spec = (root, q, cb) -> {
            List<Predicate> p = new ArrayList<>();

            if (f.getPaisId()      != null) p.add(cb.equal(root.get("paisId"),      f.getPaisId()));
            if (f.getEstadoId()    != null) p.add(cb.equal(root.get("estadoId"),    f.getEstadoId()));
            if (f.getMunicipioId() != null) p.add(cb.equal(root.get("municipioId"), f.getMunicipioId()));

            return cb.and(p.toArray(Predicate[]::new));
        };

        return oficinaRepository.findAll(spec);
    }
}
