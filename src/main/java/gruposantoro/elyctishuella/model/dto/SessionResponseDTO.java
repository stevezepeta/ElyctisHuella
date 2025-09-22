// src/main/java/gruposantoro/elyctishuella/model/dto/SessionResponseDTO.java
package gruposantoro.elyctishuella.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionResponseDTO {
    private String baseCode;
    private int count;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Long durationSeconds; // null si no se puede calcular
    private boolean completed;
    private int starts;
    private int ends;
    private int errors;

    // NUEVO
    private Long userId;
    private SessionUserDTO user;

    private List<ScanEventDTO> events;
}
