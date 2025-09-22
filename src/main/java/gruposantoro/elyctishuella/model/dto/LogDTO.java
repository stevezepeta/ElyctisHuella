package gruposantoro.elyctishuella.model.dto;


import java.time.OffsetDateTime;

public record LogDTO(
    Long id,
    OffsetDateTime date,
    String type,
    String process,
    String message,
    String errorCode,
    String sessionToken,
    String baseCode,
    PersonDTO person,
    OficinaDTO oficina
) {}