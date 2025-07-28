package gruposantoro.elyctishuella.model.dto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonEnrolledDTO {

    private Long idPerson;
    private String nombreCompleto;

    private Long oficinaId;      // ← ESTE nombre genera .oficinaId(...)
}
