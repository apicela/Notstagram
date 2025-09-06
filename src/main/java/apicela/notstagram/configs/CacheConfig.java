package apicela.notstagram.configs;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.registerCustomCache("rolesCache", buildCache(5, null).build());

        return cacheManager;
    }

    /**
     * Cria um builder de cache dinamicamente.
     *
     * @param size    quantidade máxima de entradas no cache
     * @param minutes tempo de expiração em minutos (null = sem expiração)
     * @return builder configurado
     */
    private Caffeine<Object, Object> buildCache(int size, Integer minutes) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .maximumSize(size);

        if (minutes != null) {
            builder = builder.expireAfterWrite(minutes, TimeUnit.MINUTES)
                    .removalListener((key, value, cause) -> {
                        if (cause == com.github.benmanes.caffeine.cache.RemovalCause.EXPIRED) {
                            System.out.printf("Cache expirado (%d min) - chave: %s, valor: %s%n",
                                    minutes, key, value);
                        }
                    });
        }

        return builder;
    }
}
