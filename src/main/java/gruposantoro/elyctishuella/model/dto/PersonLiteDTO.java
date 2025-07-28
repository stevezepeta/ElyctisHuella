package gruposantoro.elyctishuella.model.dto;

/**
 * DTO “ligero” para exponer sólo los datos básicos de una persona.
 * Se incluye oficinaId para que el front pueda filtrar o mostrar la oficina.
 */
public record PersonLiteDTO(
        Long id,
        String nombres,
        String primerApellido,
        String segundoApellido,
        Long oficinaId      // puede venir null
) {}
