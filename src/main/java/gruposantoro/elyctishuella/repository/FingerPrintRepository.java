package gruposantoro.elyctishuella.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import gruposantoro.elyctishuella.model.FingerPrint;
import gruposantoro.elyctishuella.model.Person;

public interface FingerPrintRepository extends JpaRepository<FingerPrint, Integer> {
    Optional<FingerPrint> findByPerson(Person person);
}
