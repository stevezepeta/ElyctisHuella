package gruposantoro.elyctishuella.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)

@Data
public class StateDTO {
    private Long id;
    private String name;
    private List<CityDTO> cities;
}
