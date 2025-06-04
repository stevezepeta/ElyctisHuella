// src/main/java/gruposantoro/elyctishuella/service/PersonService.java
package gruposantoro.elyctishuella.service;

import gruposantoro.elyctishuella.model.dto.PersonDTO;
import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

    public Person registerPerson(PersonDTO dto) {
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
