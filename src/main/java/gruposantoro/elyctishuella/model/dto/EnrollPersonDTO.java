package gruposantoro.elyctishuella.model.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class EnrollPersonDTO {
    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    private String sexo;
    private String nacionalidad;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String curp;
    // Si tienes otros campos, agrégalos aquí
}
