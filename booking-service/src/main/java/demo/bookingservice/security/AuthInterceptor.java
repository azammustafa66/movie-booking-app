package demo.bookingservice.security;

import demo.bookingservice.enums.Role;
import demo.bookingservice.exceptions.BadCredentialsException;
import demo.bookingservice.exceptions.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Gatekeeper for the booking APIs.
 * <p>
 * Identity here is delegated entirely to the upstream API gateway: the
 * gateway holds the Spring Security config, verifies the caller's JWT, and
 * forwards the request with {@code X-User-Id}/{@code X-User-Role}/
 * {@code X-User-Email} headers already attached. Booking-service never
 * decodes a token itself — it just trusts those headers, which is only
 * safe because booking-service is not reachable directly from outside the
 * gateway.
 * <p>
 * Unlike catalog-service's {@code AuthInterceptor} (which requires exactly
 * one role per path), this one takes a <em>set</em> of allowed roles — see
 * {@link demo.bookingservice.config.SecurityConfig}, which allows both
 * {@link Role#CUSTOMER} and {@link Role#ADMIN} on the same path but
 * excludes {@link Role#VENDOR}, who manage events rather than attend them.
 * <p>
 * Unlike a servlet {@code Filter}, a {@link HandlerInterceptor} runs
 * <em>inside</em> Spring MVC's dispatch, so exceptions thrown from
 * {@link #preHandle} — missing/malformed headers, disallowed role — reach
 * {@code GlobalExceptionHandler} exactly like any other service-layer
 * exception, rather than needing to be written to the response by hand
 * here.
 */
public class AuthInterceptor implements HandlerInterceptor {

    static final String USER_ID_HEADER = "X-User-Id";
    static final String USER_ROLE_HEADER = "X-User-Role";
    static final String USER_EMAIL_HEADER = "X-User-Email";

    private final Set<Role> allowedRoles;

    public AuthInterceptor(Role... allowedRoles) {
        this.allowedRoles = Set.of(allowedRoles);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        AuthenticatedUser user = extractUser(request);
        if (!allowedRoles.contains(user.role())) {
            throw new ForbiddenException("This endpoint is not available to the " + user.role() + " role");
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
        String emailHeader = request.getHeader(USER_EMAIL_HEADER);
        if (userIdHeader == null || roleHeader == null || emailHeader == null) {
            throw new BadCredentialsException(
                    "Missing " + USER_ID_HEADER + "/" + USER_ROLE_HEADER + "/" + USER_EMAIL_HEADER
                            + " header; this endpoint must be called through the API gateway");
        }
        try {
            return new AuthenticatedUser(Long.parseLong(userIdHeader), Role.valueOf(roleHeader), emailHeader);
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException(
                    "Malformed " + USER_ID_HEADER + "/" + USER_ROLE_HEADER + " header");
        }
    }
}
