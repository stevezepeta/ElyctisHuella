package gruposantoro.elyctishuella.model.dto;

import lombok.Data;

@Data
public class ScanLogFilterDTO {
    private Long personId;
    private String type;
    private String device;
    private String scanDevice;
    private String process;
    private String message;
    private String fromDate; // yyyy-MM-dd HH:mm:ss
    private String toDate;
}
