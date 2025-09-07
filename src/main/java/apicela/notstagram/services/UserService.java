package apicela.notstagram.services;

import apicela.notstagram.models.entities.AuthCode;
import apicela.notstagram.models.entities.Role;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.entities.UserRole;
import apicela.notstagram.models.requests.CompleteRegisterRequest;
import apicela.notstagram.models.requests.VerificationCode;
import apicela.notstagram.models.responses.TokenResponse;
import apicela.notstagram.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final TokenService tokenService;
    private final AuthCodeService authCodeService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleService roleService, TokenService tokenService, AuthCodeService authCodeService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.tokenService = tokenService;
        this.authCodeService = authCodeService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username);
    }

    public TokenResponse createPendingUser(String email) {
        User user = new User();
        user.setEmail(email);
        userRepository.save(user);
        //  authCodeService.generateAuthCodeAndSendEmail(user);
        return new TokenResponse(tokenService.generateTokenWithExtraAuthority(user, "PENDING_USER_TOKEN"));
    }

    public TokenResponse confirmPendingUser(User user, int verificationCode) {
        AuthCode auth = authCodeService.getAuthCodeFromUser(user);
        if(verificationCode != auth.getCode()) {
            throw new RuntimeException();
        }
        user.setVerified(true);
        userRepository.save(user);
        return new TokenResponse(tokenService.generateTokenWithExtraAuthority(user, "CONFIRMED_USER_TOKEN"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public TokenResponse completeRegister(User user, @Valid CompleteRegisterRequest completeRegisterRequest) {
        String hashedPassword = passwordEncoder.encode(completeRegisterRequest.password());
        user.setUsername(completeRegisterRequest.username());
        user.setPassword(hashedPassword);
        user.setInactive(false);
        user.setPublicProfile(completeRegisterRequest.publicProfile());
        Role basicRole = roleService.getRoleByName("BASIC");
        UserRole userRole = new UserRole(user, basicRole, 30);
        user.getUserRoles().add(userRole);
        userRepository.save(user);
        return new TokenResponse(tokenService.generateToken(user));
    }
}
