package demo.catalogservice.security;

import demo.catalogservice.enums.Role;
import demo.catalogservice.exceptions.BadCredentialsException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
 * One instance of this filter is registered per required {@link Role} (see
 * {@link demo.catalogservice.config.SecurityConfig}), each bound to only its
 * own URL prefix — one for {@code /api/v1/admin/**} requiring
 * {@link Role#ADMIN}, one for {@code /api/v1/vendor/**} requiring
 * {@link Role#VENDOR}. Every other path (the existing public read
 * endpoints) never passes through here.
 * <p>
 * This runs as a servlet {@link jakarta.servlet.Filter}, ahead of Spring
 * MVC's dispatcher, so exceptions thrown here are <em>not</em> seen by
 * {@code GlobalExceptionHandler} — auth failures are written to the response
 * directly instead of being thrown. Authorization failures that depend on
 * looking up a specific resource (e.g. a vendor touching a theatre they
 * don't own) can't be decided here and are instead thrown as
 * {@link demo.catalogservice.exceptions.ForbiddenException} from within the
 * controller/service call, where {@code GlobalExceptionHandler} does apply.
 */
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    /** Request attribute the resolved {@link AuthenticatedUser} is stored under for {@link CurrentUser} to read. */
    static final String CURRENT_USER_ATTRIBUTE = HeaderAuthenticationFilter.class.getName() + ".CURRENT_USER";

    static final String USER_ID_HEADER = "X-User-Id";
    static final String USER_ROLE_HEADER = "X-User-Role";

    private final Role requiredRole;

    public HeaderAuthenticationFilter(Role requiredRole) {
        this.requiredRole = requiredRole;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            AuthenticatedUser user = extractUser(request);
            if (user.role() != requiredRole) {
                writeError(response, HttpStatus.FORBIDDEN, "This endpoint requires the " + requiredRole + " role");
                return;
            }
            request.setAttribute(CURRENT_USER_ATTRIBUTE, user);
            filterChain.doFilter(request, response);
        } catch (BadCredentialsException e) {
            writeError(response, HttpStatus.UNAUTHORIZED, e.getMessage());
        }
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

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.getWriter().write(message);
    }
}
