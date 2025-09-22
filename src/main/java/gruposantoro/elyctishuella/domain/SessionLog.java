package gruposantoro.elyctishuella.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity
@Table(name = "session_logs", indexes = {
    @Index(name = "idx_session_logs_session_token", columnList = "session_token"),
    @Index(name = "idx_session_logs_event_date", columnList = "event_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "message")
@Accessors(chain = true) // quítalo si no quieres setters encadenables
public class SessionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Log principal */
    @Column(name = "event_date", nullable = false)
    private OffsetDateTime date;

    @Column(name = "type", length = 32)
    private String type;

    @Column(name = "process", length = 128)
    private String process;

    @Column(name = "message", length = 2000)
    private String message;

    @Column(name = "error_code", length = 128)
    private String errorCode;

    @Column(name = "session_token", length = 128, nullable = false)
    private String sessionToken;

    @Column(name = "base_code", length = 64)
    private String baseCode;

    /** Persona (alineado a tu PersonDTO) */
    @Column(name = "person_curp", length = 32)
    private String personCurp;

    @Column(name = "person_nombres", length = 128)
    private String personNombres;

    @Column(name = "person_primer_apellido", length = 128)
    private String personPrimerApellido;

    @Column(name = "person_segundo_apellido", length = 128)
    private String personSegundoApellido;

    @Column(name = "person_sexo", length = 16)
    private String personSexo;

    @Column(name = "person_nacionalidad", length = 64)
    private String personNacionalidad;

    @Column(name = "person_fecha_nacimiento")
    private LocalDate personFechaNacimiento;

    @Column(name = "person_direccion", length = 256)
    private String personDireccion;

    /** FK opcional a oficina (solo el id) */
    @Column(name = "oficina_id")
    private Long oficinaId;

    /** Oficina (alineado a tu OficinaDTO) */
    @Column(name = "oficina_nombre", length = 128)
    private String oficinaNombre;

    @Column(name = "oficina_direccion", length = 256)
    private String oficinaDireccion;

    @Column(name = "oficina_pais_id")
    private Long oficinaPaisId;

    @Column(name = "oficina_estado_id")
    private Long oficinaEstadoId;

    @Column(name = "oficina_municipio_id")
    private Long oficinaMunicipioId;
}
