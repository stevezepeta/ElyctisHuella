package gruposantoro.elyctishuella.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scan_date")
    private LocalDateTime date;

    @Column(name = "scan_type")
    private String type;

    @ManyToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private Person person;

    @Column(name = "device")
    private String device;

    @Column(name = "scan_device")
    private String scanDevice;

    @Column(name = "process_type")
    private String process; // INE, Passport, etc.

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
}
