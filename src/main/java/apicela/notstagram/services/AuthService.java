package apicela.notstagram.services;

import apicela.notstagram.configs.TokenSettings;
import apicela.notstagram.exceptions.EmailAlreadyInUseException;
import apicela.notstagram.exceptions.UsernameAlreadyInUseException;
import apicela.notstagram.models.entities.*;
import apicela.notstagram.models.requests.ChangePasswordRequest;
import apicela.notstagram.models.requests.CompleteRegisterRequest;
import apicela.notstagram.models.requests.LoginRequest;
import apicela.notstagram.models.responses.AuthResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserService userService;
    private final RoleService roleService;
    private final TokenService tokenService;
    private final AuthCodeService authCodeService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final TokenSettings tokenSettings;

    public AuthService(UserService userService, RoleService roleService, TokenService tokenService, AuthCodeService authCodeService, RefreshTokenService refreshTokenService, PasswordEncoder passwordEncoder, TokenSettings tokenSettings) {
        this.userService = userService;
        this.roleService = roleService;
        this.tokenService = tokenService;
        this.authCodeService = authCodeService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.tokenSettings = tokenSettings;
    }

    @Transactional
    public AuthResponse createPendingUser(String email) {
        if (userService.existsByEmail(email))
            throw new EmailAlreadyInUseException("E-mail already in use");

        User user = new User();
        user.setEmail(email);
        userService.save(user);
        authCodeService.generateAuthCodeAndSendEmail(user);
        String token = tokenService.generateTokenWithExtraAuthority(user, "PENDING_USER_TOKEN");
        return new AuthResponse(token, null, tokenSettings.getAccessToken().getExpirationSeconds());
    }

    @Transactional
    public AuthResponse confirmPendingUser(User user, int verificationCode) {
        AuthCode auth = authCodeService.getAuthCodeFromUser(user);
        if (verificationCode != auth.getCode()) {
            throw new BadCredentialsException("Invalid verification code");
        }
        user.setVerified(true);
        userService.save(user);
        String token = tokenService.generateTokenWithExtraAuthority(user, "CONFIRMED_USER_TOKEN");
        return new AuthResponse(token, null, tokenSettings.getAccessToken().getExpirationSeconds());
    }


    @Transactional
    public AuthResponse completeRegister(User user, @Valid CompleteRegisterRequest completeRegisterRequest) {
        if (userService.existsByUsername(completeRegisterRequest.username()))
            throw new UsernameAlreadyInUseException("Username already in use");

        String hashedPassword = passwordEncoder.encode(completeRegisterRequest.password());
        user.setUsername(completeRegisterRequest.username());
        user.setPassword(hashedPassword);
        user.setInactive(false);
        user.setPublicProfile(completeRegisterRequest.publicProfile());
        Role basicRole = roleService.getRoleByName("BASIC");
        UserRole userRole = new UserRole(user, basicRole, tokenSettings.getRolePermission().getExpirationSeconds());
        user.getUserRoles().add(userRole);
        userService.save(user);
        return login(new LoginRequest(user.getEmail(), completeRegisterRequest.password()));
    }

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        User user = userService.findByEmail(loginRequest.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (user.isInactive()) {
            userService.activateUser(user);
        }
        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String accessToken = tokenService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                tokenSettings.getAccessToken().getExpirationSeconds()
        );
    }

    @Transactional
    public AuthResponse resetPassword(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email"));
        authCodeService.generateAuthCodeAndSendEmail(user);
        String token = tokenService.generateTokenWithExtraAuthority(user, "RESET_PASSWORD");
        return new AuthResponse(token, null, tokenSettings.getAccessToken().getExpirationSeconds());
    }

    @Transactional
    public AuthResponse changePassword(User user, ChangePasswordRequest changePasswordRequest) {
        user.setPassword(passwordEncoder.encode(changePasswordRequest.password()));
        userService.save(user);
        return login(new LoginRequest(user.getEmail(), changePasswordRequest.password()));
    }
}
