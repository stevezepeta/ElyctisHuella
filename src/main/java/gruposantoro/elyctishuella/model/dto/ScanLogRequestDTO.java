package gruposantoro.elyctishuella.model.dto;

import lombok.Data;

@Data
public class ScanLogRequestDTO {

    private String scanDate;        // "yyyy-MM-dd HH:mm:ss"
    private String type;
    private String device;
    private String scanDevice;
    private String process;
    private String message;

    private Long personId;          // opcional
    private Long oficinaId;         // ✔️ Único ID de ubicación
    private Long trackingId;  // nuevo campo

      private String errorCode;       
    private String sessionToken;    
    private String baseCode;
}
