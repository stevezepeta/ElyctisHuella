package gruposantoro.elyctishuella.model.dto;

import lombok.Data;

@Data
public class ScanLogRequestDTO {
    private Long personId;
    private String type;
    private String device;
    private String scanDevice;
    private String process;
    private String message;
    private String scanDate; 
}
