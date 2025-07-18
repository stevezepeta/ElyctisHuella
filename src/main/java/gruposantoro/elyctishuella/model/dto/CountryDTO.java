package gruposantoro.elyctishuella.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryDTO {
    private Long id;
    private String name;
    private List<StateDTO> states;
}
