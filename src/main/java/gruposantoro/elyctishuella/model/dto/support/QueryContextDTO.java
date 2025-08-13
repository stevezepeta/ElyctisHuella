package gruposantoro.elyctishuella.model.dto.support;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QueryContextDTO {
    private String code;
    private String session; // baseCode opcional
    private String device;
    private String user;

    public static QueryContextDTO of(String code, String session, String device, String user) {
        return new QueryContextDTO(code, session, device, user);
    }
}
