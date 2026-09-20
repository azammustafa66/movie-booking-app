package demo.discoveryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Standalone Eureka registry — every other service in this repo registers
 * with and discovers each other through this one node (see {@code EUREKA_URL}
 * in each service's own config, defaulting to this service's port, 8084).
 * {@code @EnableEurekaServer} is the only thing that makes this a registry
 * rather than a client; see {@code application.yaml} for why it's configured
 * to never register with or query itself.
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServiceApplication.class, args);
    }

}
