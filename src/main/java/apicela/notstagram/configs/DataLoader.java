package apicela.notstagram.configs;

import apicela.notstagram.models.entities.Role;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.CompleteRegisterRequest;
import apicela.notstagram.repositories.RoleRepository;
import apicela.notstagram.services.AuthCodeService;
import apicela.notstagram.services.AuthService;
import apicela.notstagram.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AuthService authService;
    private final AuthCodeService authCodeService;
    private final UserService userService;

    public DataLoader(RoleRepository roleRepository, AuthService authService, AuthCodeService authCodeService, UserService userService) {
        this.roleRepository = roleRepository;
        this.authService = authService;
        this.authCodeService = authCodeService;
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            roleRepository.save(adminRole);
        }

        if (roleRepository.findByName("BASIC").isEmpty()) {
            Role basicRole = new Role();
            basicRole.setName("BASIC");
            roleRepository.save(basicRole);
        }
        String email = "jamilnetobr@gmail.com";
        authService.createPendingUser(email);
        User u = userService.getUserByEmail(email);
        int verificationCode = authCodeService.getAuthCodeFromUser(u).getCode();
        authService.confirmPendingUser(u, verificationCode);
        CompleteRegisterRequest completeRegisterRequest = new CompleteRegisterRequest("apicela", "123", true);
        authService.completeRegister(u, completeRegisterRequest);

        String email2 = "jamilnetobr2@gmail.com";
        authService.createPendingUser(email2);
        User u2 = userService.getUserByEmail(email2);
        int verificationCode2 = authCodeService.getAuthCodeFromUser(u2).getCode();
        authService.confirmPendingUser(u2, verificationCode2);
        CompleteRegisterRequest completeRegisterRequest2 = new CompleteRegisterRequest("apicela2", "123", true);
        authService.completeRegister(u2, completeRegisterRequest2);
    }
}