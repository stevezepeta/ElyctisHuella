package gruposantoro.elyctishuella.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import gruposantoro.elyctishuella.model.ScanLog;

public interface ScanLogRepository extends JpaRepository<ScanLog, Long> {

    List<ScanLog> findAllByDateBetween(LocalDateTime from, LocalDateTime to);

    // Fechas únicas sin filtro
    @Query("SELECT DISTINCT FUNCTION('DATE', s.date) FROM ScanLog s ORDER BY FUNCTION('DATE', s.date)")
    List<LocalDate> findDistinctLogDates();

    // Fechas únicas dentro de un rango
    @Query("SELECT DISTINCT FUNCTION('DATE', s.date) FROM ScanLog s WHERE s.date >= :from AND s.date <= :to ORDER BY FUNCTION('DATE', s.date)")
    List<LocalDate> findDistinctLogDatesBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // ✅ NUEVO: Todas las fechas (sin DISTINCT) dentro de un rango (para agrupación por mes y día)
  
    @Query("SELECT FUNCTION('DATE', s.date) FROM ScanLog s WHERE s.date >= :from AND s.date <= :to")
List<java.sql.Date> findAllDatesBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

@Query("SELECT DISTINCT FUNCTION('DATE', s.date) FROM ScanLog s")
List<java.sql.Date> findAllLogDates();

}
