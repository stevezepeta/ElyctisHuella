package gruposantoro.elyctishuella.model.dto.support;


import gruposantoro.elyctishuella.model.dto.ErrorCodeResponseDTO;
import gruposantoro.elyctishuella.model.dto.SessionResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SupportResponseDTO {
    private QueryContextDTO query;         // eco de los parámetros
    private ErrorCodeResponseDTO error;    // detalle del errorCode (requerido si 200)
    private SessionResponseDTO session;    // detalle de la sesión (si se pidió y existe)
    private String message;                // mensajes de error/estado opcionales
    private String status;                 // ok | not_found | bad_request
}