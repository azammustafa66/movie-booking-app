package demo.catalogservice.security;

import demo.catalogservice.enums.Role;

/**
 * The caller identity forwarded by the API gateway: who they are and what
 * role they hold. Resolved by {@link AuthInterceptor} (from the
 * {@code X-User-Id}/{@code X-User-Role} headers the gateway sets after
 * verifying the caller's JWT) and made available to the rest of the request
 * via {@link AuthContextHolder#getCurrentUser()}.
 *
 * @param userId the {@code app_users.id} of the caller, from {@code X-User-Id}
 * @param role   the caller's role, from {@code X-User-Role}
 */
public record AuthenticatedUser(Long userId, Role role) {
}
