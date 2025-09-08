package apicela.notstagram.services;

import apicela.notstagram.configs.TokenSettings;
import apicela.notstagram.models.entities.*;
import apicela.notstagram.models.requests.CompleteRegisterRequest;
import apicela.notstagram.models.requests.LoginRequest;
import apicela.notstagram.models.responses.AuthResponse;
import apicela.notstagram.repositories.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final TokenService tokenService;
    private final AuthCodeService authCodeService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final TokenSettings tokenSettings;
    public AuthService(UserRepository userRepository, RoleService roleService, TokenService tokenService, AuthCodeService authCodeService, RefreshTokenService refreshTokenService, PasswordEncoder passwordEncoder, TokenSettings tokenSettings) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.tokenService = tokenService;
        this.authCodeService = authCodeService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.tokenSettings = tokenSettings;
    }

    @Transactional
    public AuthResponse createPendingUser(String email) {
        User user = new User();
        user.setEmail(email);
        userRepository.save(user);
        //  authCodeService.generateAuthCodeAndSendEmail(user);
        String token = tokenService.generateTokenWithExtraAuthority(user, "PENDING_USER_TOKEN");
        return new AuthResponse(token, null, tokenSettings.getAccessToken().getExpirationSeconds());
    }

    @Transactional
    public AuthResponse confirmPendingUser(User user, int verificationCode) {
        AuthCode auth = authCodeService.getAuthCodeFromUser(user);
        if(verificationCode != auth.getCode()) {
            throw new RuntimeException();
        }
        user.setVerified(true);
        userRepository.save(user);
        String token = tokenService.generateTokenWithExtraAuthority(user, "CONFIRMED_USER_TOKEN");
        return new AuthResponse(token, null, tokenSettings.getAccessToken().getExpirationSeconds());
    }


    @Transactional
    public AuthResponse completeRegister(User user, @Valid CompleteRegisterRequest completeRegisterRequest) {
        String hashedPassword = passwordEncoder.encode(completeRegisterRequest.password());
        user.setUsername(completeRegisterRequest.username());
        user.setPassword(hashedPassword);
        user.setInactive(false);
        user.setPublicProfile(completeRegisterRequest.publicProfile());
        Role basicRole = roleService.getRoleByName("BASIC");
        UserRole userRole = new UserRole(user, basicRole, tokenSettings.getRolePermission().getExpirationSeconds());
        user.getUserRoles().add(userRole);
        userRepository.save(user);
        return login(new LoginRequest(user.getEmail(), completeRegisterRequest.password()));
    }

    public AuthResponse login(LoginRequest loginRequest){
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new RuntimeException("Senha inválida");
        }

        String accessToken = tokenService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                tokenSettings.getAccessToken().getExpirationSeconds()
        );
    }
}
