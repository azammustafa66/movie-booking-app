package demo.userservice.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** General-purpose application beans shared across services and controllers. */
@Configuration
public class AppConfig {

    /** Used to map entities to their DTO representations (e.g. {@code AppUser} -> {@code SignUpResponseDto}). */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /** BCrypt-based password hashing for storing and verifying user credentials. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
