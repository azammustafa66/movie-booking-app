package demo.catalogservice.security;

/**
 * Holds the {@link AuthenticatedUser} for the current request's thread.
 * <p>
 * Set by {@link AuthInterceptor#preHandle} once a request passes
 * authentication, and read anywhere downstream — controller or service —
 * via {@link #getCurrentUser()}. {@link #set} and {@link #clear} are
 * package-private: only the interceptor is meant to manage the lifecycle.
 * <p>
 * Clearing matters more than it looks: Tomcat reuses worker threads across
 * requests, so a request that set a user here without clearing it afterward
 * would leak that identity into whatever unrelated request the same thread
 * picks up next. {@link AuthInterceptor#afterCompletion} clears it for
 * exactly this reason — Spring guarantees that callback runs once the
 * request completes, mirroring a {@code finally} block.
 */
public final class AuthContextHolder {

    private static final ThreadLocal<AuthenticatedUser> CURRENT_USER = new ThreadLocal<>();

    private AuthContextHolder() {
    }

    static void set(AuthenticatedUser user) {
        CURRENT_USER.set(user);
    }

    static void clear() {
        CURRENT_USER.remove();
    }

    /**
     * @return the caller identity {@link AuthInterceptor} resolved for this request
     * @throws IllegalStateException if called on a thread with no authenticated user set, i.e.
     *                                outside a request routed through {@link AuthInterceptor}
     *                                (not under {@code /api/v1/admin/**} or {@code /api/v1/vendor/**})
     */
    public static AuthenticatedUser getCurrentUser() {
        AuthenticatedUser user = CURRENT_USER.get();
        if (user == null) {
            throw new IllegalStateException(
                    "No authenticated user on this thread; is this endpoint routed through AuthInterceptor?");
        }
        return user;
    }
}
