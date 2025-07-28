package gruposantoro.elyctishuella.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "oficinas")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})   // ⬅️  ¡añádelo!
public class Oficina {

    /* ───────────── Clave primaria ───────────── */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ───────────── Datos descriptivos ────────── */
    private String nombre;          // Oficina Central, Sucursal X…

    private String direccion;       // calle, número, etc.

    /* ──────── Identificadores territoriales ─── */
    @Column(name = "pais_id",     nullable = false)
    private Long paisId;            // FK → tabla países

    @Column(name = "estado_id",   nullable = false)
    private Long estadoId;          // FK → tabla estados

    @Column(name = "municipio_id", nullable = false)
    private Long municipioId;       // FK → tabla municipios
    
}
