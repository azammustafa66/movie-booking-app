package demo.userservice.enums;

/**
 * Client platform kinds for a session. Not currently enforced against
 * {@code LoginRequestDto#deviceType} / {@code UserSession#deviceType}, which
 * accept free-form strings — reserved for when device-specific behavior is needed.
 */
public enum DeviceType {
    ANDROID,
    IOS,
    DESKTOP,
    WEB
}
