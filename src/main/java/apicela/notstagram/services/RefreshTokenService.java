package apicela.notstagram.services;

import apicela.notstagram.models.entities.RefreshToken;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.repositories.RefreshTokenRepository;
import apicela.notstagram.utils.DateUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {
    RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(DateUtils.daysFromNowLocal(15));

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token não encontrado"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expirado. Faça login novamente.");
        }

        return refreshToken;
    }

    public RefreshToken rotateToken(RefreshToken oldToken) {
        refreshTokenRepository.delete(oldToken);

        RefreshToken newToken = new RefreshToken();
        newToken.setUser(oldToken.getUser());
        newToken.setToken(UUID.randomUUID().toString());
        newToken.setExpiryDate(DateUtils.daysFromNowLocal(15));

        return refreshTokenRepository.save(newToken);
    }
}
