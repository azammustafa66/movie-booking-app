package demo.catalogservice.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.registerCustomCache(
                "movies",
                Caffeine.newBuilder()
                        .expireAfterWrite(30, TimeUnit.MINUTES)
                        .maximumSize(1000)
                        .build()
        );

        cacheManager.registerCustomCache(
                "movieSearch",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .maximumSize(1000)
                        .build()
        );

        cacheManager.registerCustomCache(
                "theatresByCity",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .maximumSize(500)
                        .build()
        );

        cacheManager.registerCustomCache(
                "theatreByName",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .maximumSize(500)
                        .build()
        );

        // Locks the manager to exactly these four caches: an unknown @Cacheable/@CacheEvict
        // value now fails loudly at startup instead of Caffeine silently handing back an
        // unbounded, non-expiring cache under whatever name was actually asked for.
        cacheManager.setCacheNames(List.of("movies", "movieSearch", "theatresByCity", "theatreByName"));

        return cacheManager;
    }
}