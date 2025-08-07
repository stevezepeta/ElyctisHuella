package gruposantoro.elyctishuella.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scan_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanLog {

    /* ─────────── Clave primaria ─────────── */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ─────────── Datos del evento ────────── */
    @Column(name = "scan_date", nullable = false)
    private LocalDateTime date;                    // fecha-hora exacta

    @Column(name = "scan_type", length = 50)
    private String type;                           // START, END, MRZ…

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")                // FK opcional
    private Person person;

    private String device;                         // ej. “iPhone 13”

    @Column(name = "scan_device")
    private String scanDevice;                     // ej. “Zebra S500”

    @Column(name = "process_type")
    private String process;                        // PASSPORT, INE…

    @Column(columnDefinition = "TEXT")
    private String message;                        // descripción / error

    /* ─────── Relación con Oficina ─────── */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id")               // FK real (nullable)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Oficina oficina;

    /* ────── Campos denormalizados (opcional) ────── */
    @Column(name = "pais_id")
    private Long paisId;

    @Column(name = "estado_id")
    private Long estadoId;

    @Column(name = "municipio_id")
    private Long municipioId;
    @ManyToOne
@JoinColumn(name = "tracking_id")
private TrackingCode trackingCode;

}
