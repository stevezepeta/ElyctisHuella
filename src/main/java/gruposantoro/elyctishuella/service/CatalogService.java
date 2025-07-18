package gruposantoro.elyctishuella.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import gruposantoro.elyctishuella.model.dto.CountryDTO;
import lombok.Getter;

@Service
public class CatalogService {

    @Getter
    private final List<CountryDTO> countries;

    public CatalogService(ResourceLoader resourceLoader) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Resource resource = resourceLoader.getResource("classpath:data/paises.json");
        if (!resource.exists()) {
            throw new IOException("No se encontró el archivo paises.json en /resources/data/");
        }
        try (InputStream is = resource.getInputStream()) {
            TypeReference<List<CountryDTO>> type = new TypeReference<>() {};
            this.countries = mapper.readValue(is, type);
        }
    }

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
                        .collect(Collectors.toList()))
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
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    // DTO auxiliar para simplificar respuestas
    public static class SimpleDTO {
        private final Long id;
        private final String name;

        public SimpleDTO(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
