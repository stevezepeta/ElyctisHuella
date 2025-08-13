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
    private boolean completed;    // true si hay START y END
    private int starts;
    private int ends;
    private int errors;
    private List<ScanEventDTO> events; // ordenados ASC por fecha
}
