package gruposantoro.elyctishuella.model.dto.huellas;


import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class FingerprintResultDTO {
    private BigDecimal score;
    private BigDecimal percentage;
    private boolean match;
}
