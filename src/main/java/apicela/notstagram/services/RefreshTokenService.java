package apicela.notstagram.services;

import apicela.notstagram.configs.TokenSettings;
import apicela.notstagram.exceptions.NotFoundException;
import apicela.notstagram.models.entities.RefreshToken;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.RefreshTokenRequest;
import apicela.notstagram.models.responses.AuthResponse;
import apicela.notstagram.repositories.RefreshTokenRepository;
import apicela.notstagram.utils.DateUtils;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {
    RefreshTokenRepository refreshTokenRepository;
    TokenService tokenService;
    TokenSettings tokenSettings;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, TokenService tokenService, TokenSettings tokenSettings) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenService = tokenService;
        this.tokenSettings = tokenSettings;
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(DateUtils.secondsFromNowLocal(tokenSettings.getRefreshToken().getExpirationSeconds()));
        return refreshTokenRepository.save(refreshToken);
    }

    public void verifyExpiration(RefreshToken refreshToken) {
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new CredentialsExpiredException("Invalid refresh token. Try login again.");
        }
    }

    @Transactional
    public AuthResponse rotateToken(RefreshTokenRequest oldToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(oldToken.refreshToken())
                .orElseThrow(() -> new NotFoundException("Refresh token not found."));
        verifyExpiration(refreshToken);
        refreshTokenRepository.delete(refreshToken);

        RefreshToken newToken = new RefreshToken();
        newToken.setUser(refreshToken.getUser());
        newToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(DateUtils.secondsFromNowLocal(tokenSettings.getRefreshToken().getExpirationSeconds()));
        refreshTokenRepository.save(newToken);
        return new AuthResponse(
                tokenService.generateToken(refreshToken.getUser()),
                newToken.getToken(),
                tokenSettings.getAccessToken().getExpirationSeconds()
        );
    }
}
