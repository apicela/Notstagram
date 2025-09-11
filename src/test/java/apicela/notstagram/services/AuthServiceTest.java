package apicela.notstagram.services;

import apicela.notstagram.configs.TokenSettings;
import apicela.notstagram.exceptions.EmailAlreadyInUseException;
import apicela.notstagram.exceptions.UsernameAlreadyInUseException;
import apicela.notstagram.models.entities.*;
import apicela.notstagram.models.requests.CompleteRegisterRequest;
import apicela.notstagram.models.requests.LoginRequest;
import apicela.notstagram.models.responses.AuthResponse;
import apicela.notstagram.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthCodeService authCodeService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenSettings tokenSettings;

    @Mock
    private TokenSettings.TokenConfig accessTokenConfig;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");
        mockUser.setUsername("mockUser");

        when(tokenSettings.getAccessToken()).thenReturn(accessTokenConfig);
        when(accessTokenConfig.getExpirationSeconds()).thenReturn(900);
    }

    @Test
    void testCreatePendingUser_success() {
        String email = "newuser@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(tokenService.generateTokenWithExtraAuthority(any(), eq("PENDING_USER_TOKEN"))).thenReturn("mock-token");

        AuthResponse response = authService.createPendingUser(email);

        assertEquals("mock-token", response.acessToken());
        assertNull(response.refreshToken());
        assertEquals(900L, response.access_expires_in());

        verify(userRepository, times(1)).save(any(User.class));
        verify(authCodeService, times(1)).generateAuthCodeAndSendEmail(any(User.class));
    }

    @Test
    void testCreatePendingUser_emailAlreadyUsed() {
        String email = "used@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(EmailAlreadyInUseException.class, () -> authService.createPendingUser(email));
    }

    @Test
    void testConfirmPendingUser_success() {
        AuthCode authCode = new AuthCode();
        authCode.setCode(123456);

        when(authCodeService.getAuthCodeFromUser(mockUser)).thenReturn(authCode);
        when(tokenService.generateTokenWithExtraAuthority(any(), eq("CONFIRMED_USER_TOKEN"))).thenReturn("confirmed-token");

        AuthResponse response = authService.confirmPendingUser(mockUser, 123456);

        assertEquals("confirmed-token", response.acessToken());
        assertTrue(mockUser.isVerified());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void testConfirmPendingUser_invalidCode() {
        AuthCode authCode = new AuthCode();
        authCode.setCode(123456);

        when(authCodeService.getAuthCodeFromUser(mockUser)).thenReturn(authCode);

        assertThrows(BadCredentialsException.class, () -> authService.confirmPendingUser(mockUser, 111111));
    }

    @Test
    void testCompleteRegister_success() {
        CompleteRegisterRequest request = new CompleteRegisterRequest("newUsername", "password123", true);
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");

        Role basicRole = new Role();
        basicRole.setName("BASIC");
        when(roleService.getRoleByName("BASIC")).thenReturn(basicRole);

        // Mock do TokenSettings
        TokenSettings.TokenConfig rolePermissionConfig = new TokenSettings.TokenConfig();
        rolePermissionConfig.setExpirationSeconds(3600);
        when(tokenSettings.getRolePermission()).thenReturn(rolePermissionConfig);

        TokenSettings.TokenConfig accessTokenConfig = new TokenSettings.TokenConfig();
        accessTokenConfig.setExpirationSeconds(900);
        when(tokenSettings.getAccessToken()).thenReturn(accessTokenConfig);

        // Mock findByEmail e matches para login
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);

        // Mock dos tokens
        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken("refresh-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(mockRefreshToken);
        when(tokenService.generateToken(any())).thenReturn("access-token");

        AuthResponse response = authService.completeRegister(mockUser, request);

        assertEquals("access-token", response.acessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals(900L, response.access_expires_in());
        assertEquals("newUsername", mockUser.getUsername());
        assertEquals("hashedPassword", mockUser.getPassword());
        assertFalse(mockUser.isInactive());
        assertTrue(mockUser.isPublicProfile());
        assertEquals(1, mockUser.getUserRoles().size());
        verify(userRepository, times(1)).save(mockUser);
    }


    @Test
    void testCompleteRegister_usernameAlreadyUsed() {
        CompleteRegisterRequest request = new CompleteRegisterRequest("existingUsername", "pass", true);
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThrows(UsernameAlreadyInUseException.class, () -> authService.completeRegister(mockUser, request));
    }

    @Test
    void testLogin_success() {
        LoginRequest loginRequest = new LoginRequest(mockUser.getEmail(), "password123");
        mockUser.setPassword("hashedPassword");

        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(tokenService.generateToken(mockUser)).thenReturn("access-token");

        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken("refresh-token");
        when(refreshTokenService.createRefreshToken(mockUser)).thenReturn(mockRefreshToken);

        AuthResponse response = authService.login(loginRequest);

        assertEquals("access-token", response.acessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals(900L, response.access_expires_in());
    }

    @Test
    void testLogin_invalidPassword() {
        LoginRequest loginRequest = new LoginRequest(mockUser.getEmail(), "wrong-pass");
        mockUser.setPassword("hashedPassword");

        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong-pass", "hashedPassword")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testLogin_userNotFound() {
        LoginRequest loginRequest = new LoginRequest("notfound@example.com", "pass");
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }
}
