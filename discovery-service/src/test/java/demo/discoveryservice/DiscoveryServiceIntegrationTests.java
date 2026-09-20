package demo.discoveryservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DiscoveryServiceIntegrationTests {

    @LocalServerPort
    private int port;

    @Test
    void eurekaServerIsUpAndRunning() {
        RestClient restClient = RestClient.create("http://localhost:" + port);
        String response = restClient.get()
                .uri("/eureka/apps")
                .retrieve()
                .body(String.class);
        assertThat(response).contains("<applications");
    }
}
