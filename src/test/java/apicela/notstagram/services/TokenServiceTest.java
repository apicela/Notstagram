package apicela.notstagram.services;

import apicela.notstagram.models.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;
    private User mockUser;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();

        mockUser = new User();
        mockUser.setEmail("user@example.com");
        mockUser.setUserRoles(Collections.emptySet()); // sem roles
    }

    @Test
    void testGenerateToken_containsEmail() {
        String token = tokenService.generateToken(mockUser);
        assertNotNull(token);

        String extractedEmail = tokenService.extractEmail(token);
        assertEquals(mockUser.getEmail(), extractedEmail);
    }

    @Test
    void testGenerateTokenWithExtraAuthority_containsExtraClaim() {
        String token = tokenService.generateTokenWithExtraAuthority(mockUser, "EXTRA_ROLE");
        assertNotNull(token);

        String extractedExtra = tokenService.extractExtraAuthority(token);
        assertEquals("EXTRA_ROLE", extractedExtra);
    }

    @Test
    void testIsTokenValid_validToken() {
        String token = tokenService.generateToken(mockUser);

        assertTrue(tokenService.isTokenValid(token, mockUser));
    }

    @Test
    void testIsTokenValid_invalidEmail() {
        String token = tokenService.generateToken(mockUser);

        User anotherUser = new User();
        anotherUser.setEmail("other@example.com");

        assertFalse(tokenService.isTokenValid(token, anotherUser));
    }

    @Test
    void testIsTokenExpired_expiredToken() throws InterruptedException {
        // Cria um TokenService temporário que expira rápido
        TokenService shortLivedService = new TokenService() {
            @Override
            public String generateToken(User user) {
                return io.jsonwebtoken.Jwts.builder()
                        .setSubject(user.getEmail())
                        .setExpiration(new java.util.Date(System.currentTimeMillis() - 1000)) // já expirado
                        .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor("urkewirjiewrjwierhweiurh4124821u83eh2e21".getBytes()))
                        .compact();
            }
        };

        String expiredToken = shortLivedService.generateToken(mockUser);
        assertFalse(shortLivedService.isTokenValid(expiredToken, mockUser));
    }
}
