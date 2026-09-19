package demo.catalogservice.config;

import demo.catalogservice.enums.Role;
import demo.catalogservice.security.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Wires up auth for the admin/vendor write APIs.
 * <p>
 * There's no Spring Security here — token verification is the API gateway's
 * job. This just registers two narrowly-scoped {@link AuthInterceptor}
 * instances, one per role, each bound to only its own path pattern.
 * <p>
 * Note the {@code /**} suffix: {@link InterceptorRegistry} path patterns are
 * Ant-style, where {@code /*} matches exactly one path segment — unlike a
 * servlet {@code url-pattern}'s {@code /*}, which matches any depth. Using
 * {@code /*} here would silently stop protecting anything nested one level
 * deeper, e.g. {@code /api/v1/admin/theatres/{id}}.
 */
@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    private static final String ADMIN_PATH_PATTERN = "/api/v1/admin/**";
    private static final String VENDOR_PATH_PATTERN = "/api/v1/vendor/**";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(Role.ADMIN)).addPathPatterns(ADMIN_PATH_PATTERN);
        registry.addInterceptor(new AuthInterceptor(Role.VENDOR)).addPathPatterns(VENDOR_PATH_PATTERN);
    }
}
