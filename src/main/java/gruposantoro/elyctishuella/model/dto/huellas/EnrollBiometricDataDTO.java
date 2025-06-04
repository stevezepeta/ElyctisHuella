package gruposantoro.elyctishuella.model.dto.huellas;

import java.util.Map;

import gruposantoro.elyctishuella.model.FingerPrint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnrollBiometricDataDTO {
    private String idPerson;
    private FingerPrint.fingerStatus indexLeftStatus;
    private FingerPrint.fingerStatus middleLeftStatus;
    private FingerPrint.fingerStatus ringLeftStatus;
    private FingerPrint.fingerStatus littleLeftStatus;
    private FingerPrint.fingerStatus thumbLeftStatus;
    private FingerPrint.fingerStatus thumbRightStatus;
    private FingerPrint.fingerStatus indexRightStatus;
    private FingerPrint.fingerStatus middleRightStatus;
    private FingerPrint.fingerStatus ringRightStatus;
    private FingerPrint.fingerStatus littleRightStatus;
    private Map<String, String> fingerPrintImages;
}
