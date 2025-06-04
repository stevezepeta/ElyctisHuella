package gruposantoro.elyctishuella.model.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class PersonDTO {
    private String curp;
    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    private String sexo;
    private String nacionalidad;
    private LocalDate fechaNacimiento;
    private String direccion;
}
