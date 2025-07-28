package gruposantoro.elyctishuella.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gruposantoro.elyctishuella.service.CatalogService;
import gruposantoro.elyctishuella.service.CatalogService.SimpleDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    // Obtener todos los países (solo id y name)
    @GetMapping("/paises")
    public List<SimpleDTO> getPaises() {
        return catalogService.getAllCountries();
    }

    // Obtener estados por país (solo id y name)
    @GetMapping("/estados/{pais}")
    public List<SimpleDTO> getEstados(@PathVariable String pais) {
        return catalogService.getStatesByCountry(pais);
    }

    // Obtener ciudades por país y estado (solo id y name)
@GetMapping("/municipios/{pais}/{estado}")
public List<SimpleDTO> getMunicipios(@PathVariable String pais,
                                     @PathVariable String estado) {
    // ya es List<SimpleDTO>
    return catalogService.getCitiesByState(pais, estado);
}


}
