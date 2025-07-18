package gruposantoro.elyctishuella.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)

@Data
public class CityDTO {
    private Long id;
    private String name;
}
