package demo.apigateway;

import demo.apigateway.filter.IdentityHeaderFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class IdentityHeaderFilterTest {

    @Test
    void unauthenticatedRequest_stripsSpoofedHeaders() throws ServletException, IOException {
        SecurityContextHolder.clearContext();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "hacker");
        request.addHeader("X-User-Role", "ADMIN");
        request.addHeader("X-User-Email", "hacker@example.com");
        request.addHeader("Other-Header", "value");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        IdentityHeaderFilter filter = new IdentityHeaderFilter();
        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(argThat(req -> {
            HttpServletRequest wrapper = (HttpServletRequest) req;
            return wrapper.getHeader("X-User-Id") == null &&
                   wrapper.getHeader("X-User-Role") == null &&
                   wrapper.getHeader("X-User-Email") == null &&
                   !Collections.list(wrapper.getHeaderNames()).contains("X-User-Id") &&
                   "value".equals(wrapper.getHeader("Other-Header"));
        }), any());
    }

    @Test
    void authenticatedRequest_setsHeadersFromJwt() throws ServletException, IOException {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user-123")
                .claim("role", "CUSTOMER")
                .claim("email", "user@example.com")
                .build();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "hacker");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        IdentityHeaderFilter filter = new IdentityHeaderFilter();
        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(argThat(req -> {
            HttpServletRequest wrapper = (HttpServletRequest) req;
            return "user-123".equals(wrapper.getHeader("X-User-Id")) &&
                   "CUSTOMER".equals(wrapper.getHeader("X-User-Role")) &&
                   "user@example.com".equals(wrapper.getHeader("X-User-Email")) &&
                   Collections.list(wrapper.getHeaders("X-User-Id")).contains("user-123");
        }), any());
        
        SecurityContextHolder.clearContext();
    }
}
