package demo.userservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * A single refresh-token session for an {@link AppUser}. One row is created per
 * login and per refresh-token rotation; rotation revokes the old row rather
 * than reusing it, giving a full session history per user/device.
 */
@Entity
@Table(name = "user_sessions")
@Getter
@Setter
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SHA-256 hash of the refresh token; the raw token itself is never persisted. */
    @Column(nullable = false, unique = true)
    private String refreshTokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** Set once the session has been superseded (rotation) or explicitly logged out. */
    @Column(nullable = false)
    private boolean revoked = false;

    // Session metadata
    private String deviceType;
    @Column(length = 1000)
    private String userAgent;

    private String ipAddress;

    private LocalDateTime lastUsedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
