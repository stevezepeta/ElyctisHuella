package gruposantoro.elyctishuella.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorCodeResponseDTO {
    private String errorCode;
    private int count;
    private LocalDateTime firstSeen;
    private LocalDateTime lastSeen;
    private List<ScanEventDTO> events; 
}
