package gruposantoro.elyctishuella.model.dto.huellas;


import jakarta.validation.constraints.NotBlank;
import gruposantoro.elyctishuella.util.LocaleUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FingerprintDTO {
    @NotBlank
    private String huella1;
    @NotBlank
    private String huella2;

    private long len(String value) {
        return value != null ? value.length() : 0;
    }

    public String toLog() {
        return String.format(LocaleUtil.defaultLocale, "{huella1: %s, huella2: %s}", 
                len(huella1), len(huella2));
    }
}
