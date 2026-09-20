package demo.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * {@code jwt.secret} has no default in {@code application.yaml} — it's
 * meant to come from {@code .env}, which is gitignored, so a fresh clone or
 * CI run has no value for it. Without this override, context loading fails
 * with an unresolved placeholder before the test itself ever runs. The
 * value is padded to 32+ bytes to stay in JJWT's HS256 key-length range,
 * matching what a real secret would need even though this test never
 * signs anything with it.
 */
@SpringBootTest
@TestPropertySource(properties = "jwt.secret=test-only-secret-not-for-real-use-32bytes+")
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }

}
