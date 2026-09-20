package demo.bookingservice.config;

import demo.bookingservice.enums.Role;
import demo.bookingservice.security.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Wires up auth for the booking APIs.
 * <p>
 * There's no Spring Security here — token verification is the API gateway's
 * job. This registers one {@link AuthInterceptor} bound to the bookings
 * path, allowing {@link Role#CUSTOMER} (booking for themselves) and
 * {@link Role#ADMIN} (booking on anyone's behalf) but excluding
 * {@link Role#VENDOR} — vendors manage events through catalog-service's
 * vendor APIs, they don't attend them.
 * <p>
 * Note the {@code /**} suffix: {@link InterceptorRegistry} path patterns are
 * Ant-style, where {@code /*} matches exactly one path segment — unlike a
 * servlet {@code url-pattern}'s {@code /*}, which matches any depth. Using
 * {@code /*} here would silently stop protecting anything nested one level
 * deeper, e.g. a future {@code /api/v1/bookings/{id}/cancel}.
 */
@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    private static final String BOOKINGS_PATH_PATTERN = "/api/v1/bookings/**";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(Role.CUSTOMER, Role.ADMIN))
                .addPathPatterns(BOOKINGS_PATH_PATTERN);
    }
}
