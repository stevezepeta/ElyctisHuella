// src/main/java/gruposantoro/elyctishuella/model/dto/support/SupportResponseDTO.java
package gruposantoro.elyctishuella.model.dto.support;

import java.util.List;

import gruposantoro.elyctishuella.model.dto.ErrorCodeResponseDTO;
import gruposantoro.elyctishuella.model.dto.OfficeDTO;
import gruposantoro.elyctishuella.model.dto.ScanEventDTO;
import gruposantoro.elyctishuella.model.dto.SessionResponseDTO;
import gruposantoro.elyctishuella.model.dto.SessionUserDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupportResponseDTO {
    private QueryContextDTO     query;
    private ErrorCodeResponseDTO error;   // SIN events (metadatos)
    private SessionResponseDTO   session; // SIN events (metadatos)

    // ── NUEVOS ──
    private List<ScanEventDTO> events;    // eventos combinados (error+session), ordenados y sin duplicados
    private List<OfficeDTO>    offices;   // oficinas únicas {id, nombre}

    private Long              personId;
    private SessionUserDTO    person;

    private String message;
    private String status;                // ok | not_found | bad_request
}
