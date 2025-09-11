package apicela.notstagram.services;

import apicela.notstagram.models.entities.AuthCode;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.repositories.AuthCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthCodeServiceTest {

    @Mock
    private AuthCodeRepository authCodeRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthCodeService authCodeService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(java.util.UUID.randomUUID());
        mockUser.setEmail("test@example.com");
        mockUser.setUsername("mockUser");
    }

    @Test
    void testGetAuthCodeFromUser_existingCode() {
        AuthCode existingCode = new AuthCode();
        existingCode.setCode(123456);
        existingCode.setExpiration(LocalDateTime.now().plusMinutes(10));
        existingCode.setUser(mockUser);

        when(authCodeRepository.findLastValidByUser(eq(mockUser), any())).thenReturn(Optional.of(existingCode));

        AuthCode result = authCodeService.getAuthCodeFromUser(mockUser);

        assertEquals(existingCode, result);
        verify(authCodeRepository, never()).save(any());
    }

    @Test
    void testGetAuthCodeFromUser_generateNewCode() {
        when(authCodeRepository.findLastValidByUser(eq(mockUser), any())).thenReturn(Optional.empty());

        // Simula o save retornando o mesmo objeto
        doAnswer(invocation -> invocation.getArgument(0))
                .when(authCodeRepository).save(any(AuthCode.class));

        AuthCode result = authCodeService.getAuthCodeFromUser(mockUser);

        assertNotNull(result.getCode());
        assertEquals(mockUser, result.getUser());
        assertTrue(result.getExpiration().isAfter(java.time.LocalDateTime.now()));
        verify(authCodeRepository, times(1)).save(result);
    }

    @Test
    void testSendMail_callsEmailService() {
        AuthCode authCode = new AuthCode();
        authCode.setCode(654321);

        authCodeService.sendMail(mockUser, authCode);

        verify(emailService, times(1)).sendMail(argThat(mail ->
                mail.to().equals(mockUser.getEmail()) &&
                        mail.title().contains("Código de verificação") &&
                        mail.message().contains(String.valueOf(authCode.getCode()))
        ));
    }

    @Test
    void testGenerateAuthCodeAndSendEmail_callsGetAuthCode() {
        AuthCode authCode = new AuthCode();
        when(authCodeRepository.findLastValidByUser(eq(mockUser), any())).thenReturn(Optional.of(authCode));

        // Apenas chamar o método (no código atual sendMail está comentado)
        authCodeService.generateAuthCodeAndSendEmail(mockUser);

        verify(authCodeRepository, times(1)).findLastValidByUser(eq(mockUser), any());
    }
}
