package apicela.notstagram.services;

import apicela.notstagram.configs.TokenSettings;
import apicela.notstagram.exceptions.NotFoundException;
import apicela.notstagram.models.entities.RefreshToken;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.RefreshTokenRequest;
import apicela.notstagram.models.responses.AuthResponse;
import apicela.notstagram.repositories.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.CredentialsExpiredException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private TokenSettings tokenSettings;

    @Mock
    private TokenSettings.TokenConfig refreshTokenConfig;

    @Mock
    private TokenSettings.TokenConfig accessTokenConfig;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("mockUser");

        // Configura o mock dos token settings
        when(tokenSettings.getRefreshToken()).thenReturn(refreshTokenConfig);
        when(tokenSettings.getAccessToken()).thenReturn(accessTokenConfig);
        when(refreshTokenConfig.getExpirationSeconds()).thenReturn(3600);
        when(accessTokenConfig.getExpirationSeconds()).thenReturn(900);
    }

    @Test
    void testCreateRefreshToken_success() {
        RefreshToken savedToken = new RefreshToken();
        savedToken.setUser(mockUser);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedToken);

        RefreshToken result = refreshTokenService.createRefreshToken(mockUser);

        assertEquals(mockUser, result.getUser());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void testVerifyExpiration_notExpired() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        assertDoesNotThrow(() -> refreshTokenService.verifyExpiration(token));
    }

    @Test
    void testVerifyExpiration_expired() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(LocalDateTime.now().minusMinutes(1));

        assertThrows(CredentialsExpiredException.class,
                () -> refreshTokenService.verifyExpiration(token));
    }

    @Test
    void testRotateToken_success() {
        RefreshToken oldToken = new RefreshToken();
        oldToken.setToken("old-token");
        oldToken.setUser(mockUser);
        oldToken.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        RefreshTokenRequest request = new RefreshTokenRequest("old-token");

        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Optional.of(oldToken));
        when(tokenService.generateToken(mockUser)).thenReturn("new-access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = refreshTokenService.rotateToken(request);

        assertEquals("new-access-token", response.acessToken());
        assertNotNull(response.refreshToken());
        assertEquals(900L, response.access_expires_in());
        verify(refreshTokenRepository, times(1)).delete(oldToken);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void testRotateToken_tokenNotFound() {
        RefreshTokenRequest request = new RefreshTokenRequest("non-existent-token");

        when(refreshTokenRepository.findByToken("non-existent-token")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> refreshTokenService.rotateToken(request));
    }

    @Test
    void testRotateToken_tokenExpired() {
        RefreshToken oldToken = new RefreshToken();
        oldToken.setToken("old-token");
        oldToken.setUser(mockUser);
        oldToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));

        RefreshTokenRequest request = new RefreshTokenRequest("old-token");

        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Optional.of(oldToken));

        assertThrows(CredentialsExpiredException.class,
                () -> refreshTokenService.rotateToken(request));
    }
}
