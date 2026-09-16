package demo.userservice.dto;

/**
 * Token pair returned on successful login or refresh-token rotation.
 *
 * @param accessToken  short-lived JWT used to authenticate subsequent API calls
 * @param refreshToken opaque, longer-lived token used to obtain a new access token via {@code /refresh}
 */
public record LoginResponseDto(
        String accessToken,
        String refreshToken
) {
}
