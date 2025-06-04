// src/main/java/gruposantoro/elyctishuella/service/PersonService.java
package gruposantoro.elyctishuella.service;

import org.springframework.stereotype.Service;

import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.model.dto.PersonDTO;
import gruposantoro.elyctishuella.repository.PersonRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

     public Person registerPerson(PersonDTO dto) {
        // Verifica si la CURP ya existe
        if (personRepository.findByCurp(dto.getCurp()).isPresent()) {
            throw new IllegalArgumentException("La CURP ya está registrada.");
        }

        Person person = new Person();
        person.setCurp(dto.getCurp());
        person.setNombres(dto.getNombres());
        person.setPrimerApellido(dto.getPrimerApellido());
        person.setSegundoApellido(dto.getSegundoApellido());
        person.setSexo(dto.getSexo());
        person.setNacionalidad(dto.getNacionalidad());
        person.setFechaNacimiento(dto.getFechaNacimiento());
        person.setDireccion(dto.getDireccion());
        return personRepository.save(person);
    }

    public Person getPerson(Long id) {
        return personRepository.findById(id).orElse(null);
    }
}
