package apicela.notstagram.configs;

import apicela.notstagram.models.entities.Role;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.CompleteRegisterRequest;
import apicela.notstagram.models.requests.VerificationCode;
import apicela.notstagram.repositories.RoleRepository;
import apicela.notstagram.services.AuthCodeService;
import apicela.notstagram.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserService userService;
    private final AuthCodeService authCodeService;

    public DataLoader(RoleRepository roleRepository,  UserService userService,  AuthCodeService authCodeService) {
        this.roleRepository = roleRepository;
        this.userService = userService;
        this.authCodeService = authCodeService;
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
        userService.createPendingUser(email);
        User u = userService.getUserByEmail(email);
        int verificationCode = authCodeService.getAuthCodeFromUser(u).getCode();
        userService.confirmPendingUser(u, verificationCode);
        CompleteRegisterRequest completeRegisterRequest = new CompleteRegisterRequest("apicela","123",true);
        userService.completeRegister(u, completeRegisterRequest);
    }
}