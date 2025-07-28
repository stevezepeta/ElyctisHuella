package gruposantoro.elyctishuella.model.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDTO {

    private String  curp;
    private String  nombres;
    private String  primerApellido;
    private String  segundoApellido;
    private String  sexo;
    private String  nacionalidad;
    private LocalDate fechaNacimiento;
    private String  direccion;

    /**  Nuevo → FK a la tabla oficinas (nullable si tu regla lo permite) */
    private Long oficinaId;
}
