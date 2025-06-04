package gruposantoro.elyctishuella.model;


import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fingerprint")
@Builder
public class FingerPrint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idFingerprint;

    private String indexLeft;
    @Enumerated(EnumType.STRING)
    private fingerStatus indexLeftStatus;
    private String middleLeft;
    @Enumerated(EnumType.STRING)
    private fingerStatus middleLeftStatus;
    private String ringLeft;
    @Enumerated(EnumType.STRING)
    private fingerStatus ringLeftStatus;
    private String littleLeft;
    @Enumerated(EnumType.STRING)
    private fingerStatus littleLeftStatus;
    private String thumbLeft;
    @Enumerated(EnumType.STRING)
    private fingerStatus thumbLeftStatus;
    private String thumbRight;
    @Enumerated(EnumType.STRING)
    private fingerStatus thumbRightStatus;
    private String indexRight;
    @Enumerated(EnumType.STRING)
    private fingerStatus indexRightStatus;
    private String middleRight;
    @Enumerated(EnumType.STRING)
    private fingerStatus middleRightStatus;
    private String ringRight;
    @Enumerated(EnumType.STRING)
    private fingerStatus ringRightStatus;
    private String littleRight;
    @Enumerated(EnumType.STRING)
    private fingerStatus littleRightStatus;
    private String status;
    private LocalDateTime date;
  @ManyToOne
    @JoinColumn(name = "id_person", referencedColumnName = "id")
    private Person person;



    // @ManyToOne
    // @JoinColumn(name = "user_id_register")
    // private User userRegister;

    public enum fingerStatus {
        P, // Present
        A, // Absent
        N, // Unknown
        B, // Bandage
        O, //other
    }
}

