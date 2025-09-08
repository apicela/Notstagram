package apicela.notstagram.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class TokenSettings {
    private TokenConfig accessToken;
    private TokenConfig refreshToken;
    private TokenConfig rolePermission;
    @Data
    public static class TokenConfig {
        private int expirationSeconds;
    }
}
