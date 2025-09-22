// src/main/java/gruposantoro/elyctishuella/model/dto/OfficeDTO.java
package gruposantoro.elyctishuella.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OfficeDTO {
    private Long id;
    private String nombre;
}
