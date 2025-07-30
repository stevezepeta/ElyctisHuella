package gruposantoro.elyctishuella.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gruposantoro.elyctishuella.model.Oficina;
import gruposantoro.elyctishuella.model.dto.OficinaDTO;
import gruposantoro.elyctishuella.model.dto.OficinaFilterDTO;
import gruposantoro.elyctishuella.service.OficinaService;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints para registrar y consultar oficinas.
 *
 * GET /api/oficinas
 *   ├─ ?paisId=142                     → oficinas de todo el país
 *   ├─ ?paisId=142&estadoId=15         → oficinas del estado
 *   └─ ?paisId=142&estadoId=15&municipioId=5777 → oficinas del municipio
 *
 * POST /api/oficinas                   → alta de una oficina (solo IDs)
 */
@RestController
@RequestMapping("/api/oficinas")
@RequiredArgsConstructor
@Validated
public class OficinaController {

    private final OficinaService oficinaService;

    /* ════════ CREAR OFICINA ════════ */

 @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
             produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<List<Oficina>> crearOficinas(@RequestBody List<OficinaDTO> listaDto) {
    List<Oficina> guardadas = listaDto.stream()
        .map(oficinaService::crear)
        .toList();
    return ResponseEntity.status(HttpStatus.CREATED).body(guardadas);
}


    /* ════════ BUSCAR OFICINAS ════════ */

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<List<Oficina>> buscar(
        @RequestParam(required = false) Long paisId,
        @RequestParam(required = false) Long estadoId,
        @RequestParam(required = false) Long municipioId) {

    OficinaFilterDTO f = new OficinaFilterDTO();
    f.setPaisId(paisId);          // null ⇒ se ignora en el servicio
    f.setEstadoId(estadoId);
    f.setMunicipioId(municipioId);

    return ResponseEntity.ok(oficinaService.buscar(f));   // si todo es null → lista completa
}

}
