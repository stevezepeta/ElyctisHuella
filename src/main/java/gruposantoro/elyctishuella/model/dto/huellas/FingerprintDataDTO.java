package gruposantoro.elyctishuella.model.dto.huellas;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FingerprintDataDTO {
    @NotNull
    private Long idPerson;
    private String curp; // Debe estar este campo

    // Imágenes de huellas (base64, filePath, o null si está ausente)
    private String thumbLeft;
    private String indexLeft;
    private String middleLeft;
    private String ringLeft;
    private String littleLeft;
    private String thumbRight;
    private String indexRight;
    private String middleRight;
    private String ringRight;
    private String littleRight;

    // Estado de cada dedo (P, A, N, B, O)
    @NotNull
    private String thumbLeftStatus;
    @NotNull
    private String indexLeftStatus;
    @NotNull
    private String middleLeftStatus;
    @NotNull
    private String ringLeftStatus;
    @NotNull
    private String littleLeftStatus;
    @NotNull
    private String thumbRightStatus;
    @NotNull
    private String indexRightStatus;
    @NotNull
    private String middleRightStatus;
    @NotNull
    private String ringRightStatus;
    @NotNull
    private String littleRightStatus;

    // Estado global de las huellas, opcional
    private String status;
}
