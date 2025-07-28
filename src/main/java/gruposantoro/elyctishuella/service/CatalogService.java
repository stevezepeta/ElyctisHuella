package gruposantoro.elyctishuella.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import gruposantoro.elyctishuella.model.dto.CountryDTO;
import gruposantoro.elyctishuella.model.dto.StateDTO;
import lombok.Getter;

/**
 * Lee <b>classpath:data/paises.json</b> y expone
 * consultas de países / estados / municipios tanto por <i>nombre</i>
 * (métodos originales) como por <i>ID numérico</i> (nuevos métodos).
 */
@Service
public class CatalogService {

    @Getter
    private final List<CountryDTO> countries;

    public CatalogService(ResourceLoader loader) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Resource res = loader.getResource("classpath:data/paises.json");
        if (!res.exists()) {
            throw new IOException("No se encontró el archivo paises.json en /resources/data/");
        }
        try (InputStream is = res.getInputStream()) {
            this.countries = mapper.readValue(is, new TypeReference<>() {});
        }
    }

    /* ══════════════════ CONSULTAS POR NOMBRE (legacy) ══════════════════ */

    public List<SimpleDTO> getAllCountries() {
        return countries.stream()
                .map(c -> new SimpleDTO(c.getId(), c.getName()))
                .toList();
    }

    public List<SimpleDTO> getStatesByCountry(String countryName) {
        return countries.stream()
                .filter(c -> c.getName().equalsIgnoreCase(countryName))
                .findFirst()
                .map(c -> c.getStates().stream()
                        .map(s -> new SimpleDTO(s.getId(), s.getName()))
                        .toList())
                .orElse(List.of());
    }

    public List<SimpleDTO> getCitiesByState(String countryName, String stateName) {
        return countries.stream()
                .filter(c -> c.getName().equalsIgnoreCase(countryName))
                .flatMap(c -> c.getStates().stream())
                .filter(s -> s.getName().equalsIgnoreCase(stateName))
                .findFirst()
                .map(s -> s.getCities().stream()
                        .map(city -> new SimpleDTO(city.getId(), city.getName()))
                        .toList())
                .orElse(List.of());
    }

    /* ════════════════ NUEVAS CONSULTAS POR ID (numérico) ═══════════════ */

    /** Estados que pertenecen al país indicado. */
    public List<StateDTO> getStatesByCountryId(Long paisId) {
        return countries.stream()
                .filter(c -> Objects.equals(c.getId(), paisId))
                .findFirst()
                .map(CountryDTO::getStates)
                .orElse(List.of());
    }

    /** Ciudades/municipios que pertenecen al estado indicado. */
    public List<SimpleDTO> getCitiesByStateId(Long estadoId) {
        return countries.stream()
                .flatMap(c -> c.getStates().stream())
                .filter(s -> Objects.equals(s.getId(), estadoId))
                .findFirst()
                .map(s -> s.getCities().stream()
                        .map(c -> new SimpleDTO(c.getId(), c.getName()))
                        .toList())
                .orElse(List.of());
    }

    /** Todos los estados de todos los países (útil para búsquedas genéricas). */
    public List<StateDTO> getAllStates() {
        return countries.stream()
                .flatMap(c -> c.getStates().stream())
                .toList();
    }

    /* ═════════════════════ DTO AUXILIAR ═════════════════════ */

    /**
     * DTO compacto para respuestas (id + name).
     * Mantiene sólo getters para que Jackson lo serialice tal cual.
     */
    public record SimpleDTO(Long id, String name) {}
}
