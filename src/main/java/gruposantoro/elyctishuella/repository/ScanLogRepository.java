package gruposantoro.elyctishuella.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import gruposantoro.elyctishuella.model.ScanLog;

public interface ScanLogRepository extends JpaRepository<ScanLog, Long> {

    /* ─────────── Rangos por fecha/hora ─────────── */
    List<ScanLog> findAllByDateBetween(LocalDateTime from, LocalDateTime to);

    /* ─────────── Fechas (solo día) ─────────── */
    // Fechas únicas (toda la tabla)
    @Query("SELECT DISTINCT FUNCTION('DATE', s.date) FROM ScanLog s ORDER BY FUNCTION('DATE', s.date)")
    List<LocalDate> findDistinctLogDates();

    // Fechas únicas dentro de un rango [from, to]
    @Query("""
           SELECT DISTINCT FUNCTION('DATE', s.date)
           FROM ScanLog s
           WHERE s.date >= :from AND s.date <= :to
           ORDER BY FUNCTION('DATE', s.date)
           """)
    List<LocalDate> findDistinctLogDatesBetween(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to
    );

    // Todas las fechas (SIN DISTINCT) en rango, útil para agrupar por mes/día
    @Query("""
           SELECT FUNCTION('DATE', s.date)
           FROM ScanLog s
           WHERE s.date >= :from AND s.date <= :to
           """)
    List<java.sql.Date> findAllDatesBetween(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to
    );

    // Todas las fechas distintas (para el calendario completo)
    @Query("SELECT DISTINCT FUNCTION('DATE', s.date) FROM ScanLog s")
    List<java.sql.Date> findAllLogDates();

    /* ─────────── Consultas por códigos/tokens ─────────── */
    @Query("""
           SELECT l FROM ScanLog l
           LEFT JOIN FETCH l.person p
           LEFT JOIN FETCH l.oficina o
           WHERE l.errorCode = :errorCode
           ORDER BY l.date ASC
           """)
    List<ScanLog> findAllByErrorCodeOrderByDateAsc(@Param("errorCode") String errorCode);

    @Query("""
           SELECT l FROM ScanLog l
           LEFT JOIN FETCH l.person p
           LEFT JOIN FETCH l.oficina o
           WHERE l.baseCode = :baseCode
           ORDER BY l.date ASC
           """)
    List<ScanLog> findAllByBaseCodeOrderByDateAsc(@Param("baseCode") String baseCode);

    List<ScanLog> findBySessionTokenOrderByDateAsc(String sessionToken);

    @Query("""
           SELECT s FROM ScanLog s
           WHERE s.sessionToken = :token
           ORDER BY s.date ASC
           """)
    List<ScanLog> findAllByTokenOrdered(@Param("token") String token);
}
