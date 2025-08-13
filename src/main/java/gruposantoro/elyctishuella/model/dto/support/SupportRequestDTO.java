// src/main/java/gruposantoro/elyctishuella/model/dto/support/SupportRequestDTO.java
package gruposantoro.elyctishuella.model.dto.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body para POST /support
 * code: obligatorio (formato USR############-ERR###)
 * session: opcional (formato USR############)
 * device: opcional
 * user: opcional
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportRequestDTO {

    @NotBlank(message = "code es obligatorio")
    @Pattern(
        regexp = "^USR\\d{12}-ERR\\d{3}$",
        message = "code debe tener formato USR\\d{12}-ERR\\d{3} (ej: USR001241501001-ERR007)"
    )
    private String code;

    // Se valida solo si viene (null pasa); si envías "", fallará la validación.
    @Pattern(
        regexp = "^USR\\d{12}$",
        message = "session debe tener formato USR\\d{12} (ej: USR001241501001)"
    )
    private String session;

    private String device;
    private String user;
}
