package demo.apigateway.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

/**
 * Maps user-service's token shape onto a Spring Security authority. Spring
 * Security's default {@code JwtAuthenticationConverter} looks for a
 * {@code scope}/{@code scp} claim, but {@code demo.userservice.service.JwtService}
 * embeds the caller's role as a plain {@code role} claim (e.g.
 * {@code "role": "ADMIN"}) with no scopes at all — without this converter,
 * every authenticated request would carry zero authorities and
 * {@code hasRole(...)} would always fail. {@code "role":"ADMIN"} becomes a
 * single {@code ROLE_ADMIN} {@link SimpleGrantedAuthority}, matching what
 * {@code HttpSecurity#hasRole} expects.
 */
public class JwtRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String role = jwt.getClaimAsString("role");

        List<SimpleGrantedAuthority> authorities = role == null
                ? List.of()
                : List.of(new SimpleGrantedAuthority("ROLE_" + role));

        return new JwtAuthenticationToken(jwt, authorities);
    }
}
