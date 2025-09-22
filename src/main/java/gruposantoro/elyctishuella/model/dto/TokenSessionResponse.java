package gruposantoro.elyctishuella.model.dto;

import java.util.List;

public record TokenSessionResponse(
    List<LogDTO> data,
    List<PersonSummaryDTO> persons,
    Meta meta
) {
    public record Meta(
        String sessionToken,
        int totalLogs,
        int uniquePersons
    ) {}
}
