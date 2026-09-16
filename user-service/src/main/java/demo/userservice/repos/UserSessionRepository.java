package demo.userservice.repos;

import demo.userservice.entities.AppUser;
import demo.userservice.entities.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** Persistence access for {@link UserSession} refresh-token sessions. */
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);

    /**
     * Revokes (does not delete) every currently-active session for the given user; used by logout-all.
     */
    @Modifying
    @Query("""
    UPDATE UserSession s
    SET s.revoked = true
    WHERE s.user = :user
      AND s.revoked = false
""")
    void revokeAllByUser(@Param("user") AppUser user);
}
