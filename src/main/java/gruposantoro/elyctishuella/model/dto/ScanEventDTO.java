package gruposantoro.elyctishuella.model.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ScanEventDTO {
    private Long id;
    private LocalDateTime date;
    private String type;
    private String process;
    private String message;
    private String device;
    private String scanDevice;

    private Long personId;

    private Long oficinaId;
    private String oficinaNombre;

    private String errorCode;
    private String sessionToken;
    private String baseCode;

    private Long trackingId;
}
