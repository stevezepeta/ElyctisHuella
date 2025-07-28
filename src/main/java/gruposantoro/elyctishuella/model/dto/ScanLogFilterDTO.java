package gruposantoro.elyctishuella.model.dto;

import lombok.Data;

@Data
public class ScanLogFilterDTO {
    private Long  personId;
    private String type;
    private String device;
    private String scanDevice;
    private String process;
    private String message;
    private String fromDate;
    private String toDate;

    /* Filtros territoriales */
    private Long  paisId;
    private Long  estadoId;
    private Long  municipioId;
    private Long  oficinaId;      // ✔️ ya existe
}
