package gruposantoro.elyctishuella.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FingerprintVerificationResponseDTO {
    private boolean match;
    private String nombreCompleto;
    private Long id;
    private Long oficinaId;
}
