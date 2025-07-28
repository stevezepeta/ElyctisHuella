package gruposantoro.elyctishuella.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import gruposantoro.elyctishuella.model.Oficina;

/**
 * Repositorio de Oficinas.
 * • Extiende {@code JpaSpecificationExecutor} para permitir filtros dinámicos (Specifications).  
 * • Los métodos de consulta usan los **IDs reales** que existen en la entidad
 *   (paisId, estadoId, municipioId).
 */
public interface OficinaRepository
        extends JpaRepository<Oficina, Long>,
                JpaSpecificationExecutor<Oficina> {

    /* ──────── Consultas simples por ID ──────── */

    List<Oficina> findByPaisId(Long paisId);

    List<Oficina> findByEstadoId(Long estadoId);

    List<Oficina> findByEstadoIdAndMunicipioId(Long estadoId, Long municipioId);
}
