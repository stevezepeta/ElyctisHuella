package gruposantoro.elyctishuella.model.dto.huellas;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonEnrolledDTO {
    private Long idPerson;
    private String nombreCompleto;
}
