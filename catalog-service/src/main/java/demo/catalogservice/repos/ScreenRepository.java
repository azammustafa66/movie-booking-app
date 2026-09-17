package demo.catalogservice.repos;

import demo.catalogservice.entities.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepository extends JpaRepository<Screen, Long> {
    List<Screen> findByTheatre_Id(Long theatreId);
}
