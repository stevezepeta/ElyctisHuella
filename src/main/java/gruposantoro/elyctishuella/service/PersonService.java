package gruposantoro.elyctishuella.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gruposantoro.elyctishuella.model.Oficina;
import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.model.dto.PersonDTO;
import gruposantoro.elyctishuella.repository.OficinaRepository;
import gruposantoro.elyctishuella.repository.PersonRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository  personRepository;
    private final OficinaRepository oficinaRepository;   // ← nuevo

    /**
     * Registra una nueva persona.
     * Si la CURP ya existe o la oficina no se encuentra, lanza IllegalArgumentException.
     */
    @Transactional
    public Person registerPerson(PersonDTO dto) {

        // 1️⃣ CURP única
        if (personRepository.findByCurp(dto.getCurp()).isPresent()) {
            throw new IllegalArgumentException("La CURP ya está registrada.");
        }

        // 2️⃣ Validar oficina (puede ser opcional según tus reglas)
        // 2️⃣ Validar oficina
Oficina oficina = null;
if (dto.getOficinaId() != null) {
    oficina = oficinaRepository.findById(dto.getOficinaId())
            .orElseThrow(() -> new IllegalArgumentException(
                    "La oficina con id %d no existe".formatted(dto.getOficinaId())));
}


        // 3️⃣ Construir entidad
        Person person = new Person();
        person.setCurp(dto.getCurp());
        person.setNombres(dto.getNombres());
        person.setPrimerApellido(dto.getPrimerApellido());
        person.setSegundoApellido(dto.getSegundoApellido());
        person.setSexo(dto.getSexo());
        person.setNacionalidad(dto.getNacionalidad());
        person.setFechaNacimiento(dto.getFechaNacimiento());
        person.setDireccion(dto.getDireccion());
        person.setOficina(oficina);   // puede ser null

        // 4️⃣ Persistir y retornar
        return personRepository.save(person);
    }

    /** Obtiene una persona por ID, o null si no existe. */
    public Person getPerson(Long id) {
        return personRepository.findById(id).orElse(null);
    }
}
