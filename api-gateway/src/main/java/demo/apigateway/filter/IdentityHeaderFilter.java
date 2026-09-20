package demo.apigateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Sets {@code X-User-Id}/{@code X-User-Role}/{@code X-User-Email} on the
 * outgoing request before Gateway MVC routes it downstream. Every service
 * behind this gateway (catalog-service's {@code AuthInterceptor},
 * booking-service's own copy of the same pattern) trusts these headers
 * unconditionally — see their Javadoc — which is only safe because this
 * filter is the single place they ever get set. It always overwrites them
 * from the validated {@link JwtAuthenticationToken} left in the
 * {@code SecurityContext} by {@code BearerTokenAuthenticationFilter}
 * (registered to run first, see {@link demo.apigateway.config.SecurityConfig}),
 * and strips whatever the client itself sent under those names — on every
 * request, authenticated or not — so a caller can never spoof another
 * user's identity, role, or email just by setting the header directly.
 */
public class IdentityHeaderFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String USER_EMAIL_HEADER = "X-User-Email";

    private static final List<String> MANAGED_HEADERS =
            List.of(USER_ID_HEADER, USER_ROLE_HEADER, USER_EMAIL_HEADER);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        Map<String, String> identityHeaders = new HashMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            Jwt jwt = jwtAuthentication.getToken();
            // JwtService embeds the caller's app_users.id as the token subject, not a custom claim.
            putIfPresent(identityHeaders, USER_ID_HEADER, jwt.getSubject());
            putIfPresent(identityHeaders, USER_ROLE_HEADER, jwt.getClaimAsString("role"));
            putIfPresent(identityHeaders, USER_EMAIL_HEADER, jwt.getClaimAsString("email"));
        }

        filterChain.doFilter(new IdentityHeaderRequestWrapper(request, identityHeaders), response);
    }

    private static void putIfPresent(Map<String, String> headers, String name, String value) {
        if (value != null) {
            headers.put(name, value);
        }
    }

    /**
     * Makes the managed headers readable only as this filter computed
     * them — never as the client sent them, and never
     * absent-but-still-present-under-the-original-name. Overriding just
     * {@link #getHeader} would leave a client-supplied value visible to any
     * downstream code that enumerates {@link #getHeaderNames()} first and
     * looks the value up from there, so all three methods are overridden
     * consistently.
     */
    private static final class IdentityHeaderRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String> identityHeaders;

        IdentityHeaderRequestWrapper(HttpServletRequest request, Map<String, String> identityHeaders) {
            super(request);
            this.identityHeaders = identityHeaders;
        }

        @Override
        public String getHeader(String name) {
            if (isManagedHeader(name)) {
                return identityHeaders.get(canonicalName(name));
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (isManagedHeader(name)) {
                String value = identityHeaders.get(canonicalName(name));
                return value == null
                        ? Collections.emptyEnumeration()
                        : Collections.enumeration(List.of(value));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = new ArrayList<>();
            Enumeration<String> originalNames = super.getHeaderNames();
            while (originalNames.hasMoreElements()) {
                String name = originalNames.nextElement();
                if (!isManagedHeader(name)) {
                    names.add(name);
                }
            }
            names.addAll(identityHeaders.keySet());
            return Collections.enumeration(names);
        }

        private boolean isManagedHeader(String name) {
            return MANAGED_HEADERS.stream().anyMatch(managed -> managed.equalsIgnoreCase(name));
        }

        private String canonicalName(String name) {
            return MANAGED_HEADERS.stream()
                    .filter(managed -> managed.equalsIgnoreCase(name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Not a managed header: " + name.toLowerCase(Locale.ROOT)));
        }
    }
}
