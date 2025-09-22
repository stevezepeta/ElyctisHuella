package gruposantoro.elyctishuella.model.dto;

public record PersonSummaryDTO(
    String curp,
    String nombres,
    String primerApellido,
    String segundoApellido,
    long logsCount
) {}
