// src/main/java/gruposantoro/elyctishuella/model/dto/SessionUserDTO.java
package gruposantoro.elyctishuella.model.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Proyección de la tabla person para respuestas de sesión */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionUserDTO {
    private Long id;

    private String curp;
    private String nombres;
    private String primerApellido;
    private String segundoApellido;

    /** Conveniencia: nombres + apellidos ya concatenado (sin dobles espacios) */
    private String nombreCompleto;

    private String sexo;
    private String nacionalidad;
    private LocalDate fechaNacimiento;
    private String direccion;
}
