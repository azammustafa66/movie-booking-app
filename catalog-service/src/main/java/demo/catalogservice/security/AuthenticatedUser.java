package demo.catalogservice.security;

import demo.catalogservice.enums.Role;

/**
 * The caller identity forwarded by the API gateway: who they are and what
 * role they hold. Attached to the request by {@link HeaderAuthenticationFilter}
 * (from the {@code X-User-Id}/{@code X-User-Role} headers the gateway sets
 * after verifying the caller's JWT) and handed to controller methods via the
 * {@link CurrentUser} parameter annotation.
 *
 * @param userId the {@code app_users.id} of the caller, from {@code X-User-Id}
 * @param role   the caller's role, from {@code X-User-Role}
 */
public record AuthenticatedUser(Long userId, Role role) {
}
