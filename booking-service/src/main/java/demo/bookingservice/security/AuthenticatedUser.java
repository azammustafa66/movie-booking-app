package demo.bookingservice.security;

import demo.bookingservice.enums.Role;

/**
 * The caller identity forwarded by the API gateway: who they are, what role
 * they hold, and how to reach them. Resolved by {@link AuthInterceptor}
 * (from the {@code X-User-Id}/{@code X-User-Role}/{@code X-User-Email}
 * headers the gateway sets after verifying the caller's JWT) and made
 * available to the rest of the request via
 * {@link AuthContextHolder#getCurrentUser()}. {@code email} exists purely
 * to hand off to {@code BookingEventProducer} for booking confirmation/
 * cancellation notifications — nothing here needs it for authorization.
 *
 * @param userId the {@code app_users.id} of the caller, from {@code X-User-Id}
 * @param role   the caller's role, from {@code X-User-Role}
 * @param email  the caller's email, from {@code X-User-Email}
 */
public record AuthenticatedUser(Long userId, Role role, String email) {
}
