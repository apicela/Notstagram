package apicela.notstagram.controllers;

import apicela.notstagram.services.UserService;
import lombok.extern.log4j.Log4j2;
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




}

