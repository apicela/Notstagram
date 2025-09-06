package apicela.notstagram.controllers;

import apicela.notstagram.models.responses.DefaultApiResponse;
import apicela.notstagram.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Log4j2
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register/email")
    @Operation(summary = "CREATE", description = "Here, you can create a new object for your entity")
    public ResponseEntity<Object> registerUser(@RequestBody @Valid EmailDTO dto) {
        userService.createPendingUser(dto.email());
        DefaultApiResponse<?> response = new DefaultApiResponse<>(
                "User created successfully",
                "Um e-mail de confirmação foi enviado para continuar o cadastro.",
                HttpStatus.CREATED.value()
        );
        log.info("{}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    record EmailDTO(@Email(message = "E-mail invalid") String email){}
}

