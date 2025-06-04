package gruposantoro.elyctishuella.repository;

import gruposantoro.elyctishuella.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByCurp(String curp);
}
