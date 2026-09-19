package demo.catalogservice.security;

import demo.catalogservice.enums.Role;
import demo.catalogservice.exceptions.BadCredentialsException;
import demo.catalogservice.exceptions.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Gatekeeper for the write-side admin/vendor APIs.
 * <p>
 * Identity here is delegated entirely to the upstream API gateway: the
 * gateway holds the Spring Security config, verifies the caller's JWT, and
 * forwards the request with {@code X-User-Id}/{@code X-User-Role} headers
 * already attached. Catalog-service never decodes a token itself — it just
 * trusts those two headers, which is only safe because catalog-service is
 * not reachable directly from outside the gateway.
 * <p>
 * One instance of this is registered per required {@link Role} (see
 * {@link demo.catalogservice.config.SecurityConfig}), each bound to only its
 * own path pattern — one for {@code /api/v1/admin/**} requiring
 * {@link Role#ADMIN}, one for {@code /api/v1/vendor/**} requiring
 * {@link Role#VENDOR}. Every other path (the existing public read
 * endpoints) never runs through this at all.
 * <p>
 * Unlike a servlet {@code Filter}, a {@link HandlerInterceptor} runs
 * <em>inside</em> Spring MVC's dispatch, so exceptions thrown from
 * {@link #preHandle} — missing/malformed headers, wrong role — reach
 * {@code GlobalExceptionHandler} exactly like any other service-layer
 * exception, rather than needing to be written to the response by hand
 * here. Authorization failures that depend on looking up a specific
 * resource (e.g. a vendor touching a theatre they don't own) still can't be
 * decided this early — those are thrown as {@link ForbiddenException} from
 * within the controller/service call instead.
 */
public class AuthInterceptor implements HandlerInterceptor {

    static final String USER_ID_HEADER = "X-User-Id";
    static final String USER_ROLE_HEADER = "X-User-Role";

    private final Role requiredRole;

    public AuthInterceptor(Role requiredRole) {
        this.requiredRole = requiredRole;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        AuthenticatedUser user = extractUser(request);
        if (user.role() != requiredRole) {
            throw new ForbiddenException("This endpoint requires the " + requiredRole + " role");
        }
        // Only reached once validation has fully succeeded, so afterCompletion below —
        // which Spring only calls when preHandle returned true — always has something to clear.
        AuthContextHolder.set(user);
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContextHolder.clear();
    }

    private AuthenticatedUser extractUser(HttpServletRequest request) {
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        String roleHeader = request.getHeader(USER_ROLE_HEADER);
        if (userIdHeader == null || roleHeader == null) {
            throw new BadCredentialsException(
                    "Missing " + USER_ID_HEADER + "/" + USER_ROLE_HEADER
                            + " header; this endpoint must be called through the API gateway");
        }
        try {
            return new AuthenticatedUser(Long.parseLong(userIdHeader), Role.valueOf(roleHeader));
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException(
                    "Malformed " + USER_ID_HEADER + "/" + USER_ROLE_HEADER + " header");
        }
    }
}
