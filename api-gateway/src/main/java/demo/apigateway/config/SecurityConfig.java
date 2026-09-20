package demo.apigateway.config;

import demo.apigateway.filter.IdentityHeaderFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * The gateway's only auth logic: verify the caller's JWT (signature +
 * expiration, via {@link JwtConfig}'s {@code JwtDecoder}) and translate it
 * into the {@code X-User-Id}/{@code X-User-Role}/{@code X-User-Email}
 * headers every downstream service trusts (see {@link IdentityHeaderFilter}).
 * Role checks here are
 * coarse and duplicate nothing service-specific: fine-grained rules (a
 * vendor only touching their own theatre, a customer only cancelling their
 * own booking) stay downstream, where the data actually lives.
 * <p>
 * Path rules mirror what's actually mounted where — see each route's
 * {@code @RequestMapping} — not a guess: catalog-service's browse
 * controllers (movies/shows/theatres/screens/seats/genres) are genuinely
 * public with no auth check of their own; {@code /api/v1/admin/**} and
 * {@code /api/v1/vendor/**} require {@code ADMIN}/{@code VENDOR}; booking
 * only requires <em>some</em> valid identity, since booking-service's own
 * {@code AuthInterceptor} already narrows that further to
 * {@code CUSTOMER}/{@code ADMIN}; {@code /internal/**} is denied outright
 * even though no route to it exists either (belt and suspenders — see
 * {@code demo.catalogservice.controller.internal.InternalCatalogController}).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Signup/login/refresh/logout — none of user-service's own endpoints
                        // require a prior token, there's no authenticated-only action there.
                        .requestMatchers("/api/v1/user/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/internal/**").denyAll()
                        .requestMatchers(
                                "/api/v1/movies/**", "/api/v1/shows/**", "/api/v1/theatres/**",
                                "/api/v1/screens/**", "/api/v1/seats/**", "/api/v1/genres/**"
                        ).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/vendor/**").hasRole("VENDOR")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtRoleConverter())))
                .addFilterAfter(new IdentityHeaderFilter(), BearerTokenAuthenticationFilter.class)
                .build();
    }
}
