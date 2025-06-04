// src/main/java/gruposantoro/elyctishuella/controller/PersonController.java
package gruposantoro.elyctishuella.controller;

import gruposantoro.elyctishuella.model.dto.PersonDTO;
import gruposantoro.elyctishuella.model.Person;
import gruposantoro.elyctishuella.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/person")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @PostMapping("/register")
    public ResponseEntity<Person> registerPerson(@RequestBody PersonDTO dto) {
        Person person = personService.registerPerson(dto);
        return ResponseEntity.ok(person);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> getPerson(@PathVariable Long id) {
        Person person = personService.getPerson(id);
        if (person == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(person);
    }
}
