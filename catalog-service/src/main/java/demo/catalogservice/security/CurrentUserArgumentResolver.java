package demo.catalogservice.security;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Injects the {@link AuthenticatedUser} that {@link HeaderAuthenticationFilter}
 * attached to the request into any controller parameter annotated
 * {@link CurrentUser}.
 */
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && AuthenticatedUser.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        Object user = webRequest.getAttribute(
                HeaderAuthenticationFilter.CURRENT_USER_ATTRIBUTE, NativeWebRequest.SCOPE_REQUEST);
        if (user == null) {
            // Should be unreachable: this resolver only runs on endpoints under
            // /api/v1/admin/** or /api/v1/vendor/**, which HeaderAuthenticationFilter
            // always populates the attribute for before the request reaches here.
            throw new IllegalStateException(
                    "@CurrentUser could not be resolved; is this endpoint routed through HeaderAuthenticationFilter?");
        }
        return user;
    }
}
