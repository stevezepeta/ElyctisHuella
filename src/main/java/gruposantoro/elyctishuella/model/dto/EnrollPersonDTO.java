package gruposantoro.elyctishuella.model.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class EnrollPersonDTO {

    private String curp;
    private String nombres;
    private String primerApellido;
    private String segundoApellido;

    private LocalDate fechaNacimiento;
    private String sexo;
    private String nacionalidad;
    private String direccion;

}
