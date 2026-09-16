package demo.userservice.repos;

import demo.userservice.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Persistence access for {@link AppUser} accounts. */
public interface UserRepository extends JpaRepository<AppUser, Long> {
    boolean existsByEmail(String email);
    Optional<AppUser> findByEmail(String email);
}
