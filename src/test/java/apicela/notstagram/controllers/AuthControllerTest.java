package apicela.notstagram.controllers;

import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.CompleteRegisterRequest;
import apicela.notstagram.models.requests.LoginRequest;
import apicela.notstagram.models.requests.RefreshTokenRequest;
import apicela.notstagram.models.requests.VerificationCode;
import apicela.notstagram.models.responses.AuthResponse;
import apicela.notstagram.services.AuthService;
import apicela.notstagram.services.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthController authController;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setEmail("user@example.com");
        mockUser.setUsername("mockUser");
    }

    @Test
    void testLogin_success() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        AuthResponse mockResponse = new AuthResponse("access-token", "refresh-token", 900);

        when(authService.login(request)).thenReturn(mockResponse);

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(authService, times(1)).login(request);
    }

    @Test
    void testRegisterUser_success() {
        AuthController.EmailDTO emailDTO = new AuthController.EmailDTO("newuser@example.com");
        AuthResponse mockResponse = new AuthResponse("pending-token", null, 900);

        when(authService.createPendingUser(emailDTO.email())).thenReturn(mockResponse);

        ResponseEntity<AuthResponse> response = authController.registerUser(emailDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(authService, times(1)).createPendingUser(emailDTO.email());
    }

    @Test
    void testConfirmEmail_success() {
        int code = 123456;
        AuthResponse mockResponse = new AuthResponse("confirmed-token", null, 900);

        when(authService.confirmPendingUser(mockUser, code)).thenReturn(mockResponse);

        ResponseEntity<AuthResponse> response = authController.confirmEmail(mockUser, new VerificationCode(code));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(authService, times(1)).confirmPendingUser(mockUser, code);
    }

    @Test
    void testCompleteRegister_success() {
        CompleteRegisterRequest request = new CompleteRegisterRequest("newUsername", "password123", true);
        AuthResponse mockResponse = new AuthResponse("access-token", "refresh-token", 900);

        when(authService.completeRegister(mockUser, request)).thenReturn(mockResponse);

        ResponseEntity<AuthResponse> response = authController.completeRegister(mockUser, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(authService, times(1)).completeRegister(mockUser, request);
    }

    @Test
    void testRefreshToken_success() {
        RefreshTokenRequest request = new RefreshTokenRequest("old-refresh-token");
        AuthResponse mockResponse = new AuthResponse("new-access-token", "new-refresh-token", 900);

        when(refreshTokenService.rotateToken(request)).thenReturn(mockResponse);

        ResponseEntity<AuthResponse> response = authController.refreshToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(refreshTokenService, times(1)).rotateToken(request);
    }

}
