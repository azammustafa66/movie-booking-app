package demo.catalogservice.config;

import demo.catalogservice.enums.Role;
import demo.catalogservice.security.CurrentUserArgumentResolver;
import demo.catalogservice.security.HeaderAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Wires up auth for the admin/vendor write APIs.
 * <p>
 * There's no Spring Security here — token verification is the API gateway's
 * job. This just registers two narrowly-scoped {@link HeaderAuthenticationFilter}
 * instances, one per role, each bound to only its own URL prefix, plus the
 * {@link CurrentUserArgumentResolver} that lets controllers accept the
 * resulting identity as a plain method parameter.
 */
@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    private static final String ADMIN_PATH_PATTERN = "/api/v1/admin/*";
    private static final String VENDOR_PATH_PATTERN = "/api/v1/vendor/*";

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver());
    }

    /** Requires {@code X-User-Role: ADMIN} for every {@code /api/v1/admin/**} request. */
    @Bean
    public FilterRegistrationBean<HeaderAuthenticationFilter> adminAuthFilter() {
        FilterRegistrationBean<HeaderAuthenticationFilter> registration =
                new FilterRegistrationBean<>(new HeaderAuthenticationFilter(Role.ADMIN));
        registration.addUrlPatterns(ADMIN_PATH_PATTERN);
        registration.setName("adminAuthFilter");
        return registration;
    }

    /** Requires {@code X-User-Role: VENDOR} for every {@code /api/v1/vendor/**} request. */
    @Bean
    public FilterRegistrationBean<HeaderAuthenticationFilter> vendorAuthFilter() {
        FilterRegistrationBean<HeaderAuthenticationFilter> registration =
                new FilterRegistrationBean<>(new HeaderAuthenticationFilter(Role.VENDOR));
        registration.addUrlPatterns(VENDOR_PATH_PATTERN);
        registration.setName("vendorAuthFilter");
        return registration;
    }
}
