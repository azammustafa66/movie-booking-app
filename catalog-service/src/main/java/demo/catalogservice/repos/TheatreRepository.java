package demo.catalogservice.repos;

import demo.catalogservice.entities.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TheatreRepository extends JpaRepository<Theatre, Long> {
    /** "Pick your city" step: theatres available in the city the user selected. */
    List<Theatre> findByCityIgnoreCase(String city);

    /** Search-bar lookup as the user types a theatre name. */
    List<Theatre> findByNameContainingIgnoreCase(String name);
}
