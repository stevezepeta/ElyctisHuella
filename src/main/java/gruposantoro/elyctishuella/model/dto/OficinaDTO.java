package gruposantoro.elyctishuella.model.dto;

import lombok.Data;

@Data
public class OficinaDTO {
    private String nombre;
    private String direccion;

    private Long paisId;
    private Long estadoId;
    private Long municipioId;
}
