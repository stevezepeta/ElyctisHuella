package gruposantoro.elyctishuella.service;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Maneja la excepción personalizada
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of("success", false, "message", ex.getMessage()));
    }

    // Maneja errores de integridad de datos de Hibernate/JPA
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "El registro ya existe o hay un dato duplicado.";
        // Puedes inspeccionar el mensaje de error para personalizar aún más
        if (ex.getRootCause() != null && ex.getRootCause().getMessage() != null &&
            ex.getRootCause().getMessage().contains("for key 'person.UK")) {
            message = "La CURP ya está registrada.";
        }
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of("success", false, "message", message));
    }
}
