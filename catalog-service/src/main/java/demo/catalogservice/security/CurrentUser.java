package demo.catalogservice.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an {@link AuthenticatedUser} controller parameter to be resolved from
 * the request attribute {@link HeaderAuthenticationFilter} set while
 * authenticating the call. Only valid on endpoints reachable through
 * {@link HeaderAuthenticationFilter} (i.e. under {@code /api/v1/admin/**} or
 * {@code /api/v1/vendor/**}); resolving it anywhere else throws.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}
