package apicela.notstagram.controllers;

import apicela.notstagram.models.entities.User;
import apicela.notstagram.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class UserControllerTest {

    private static final String USERNAME_TO_FOLLOW = "testuser";
    @Mock
    private UserService userService;
    @InjectMocks
    private UserController userController;
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Criando um usuário mock
        mockUser = new User();
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("password");
        mockUser.setUsername("mockUser");
        mockUser.setInactive(false);
    }

    @Test
    void testFollowUser() {
        ResponseEntity<Void> response = userController.followUser(mockUser, USERNAME_TO_FOLLOW);

        verify(userService, times(1)).followUser(mockUser, USERNAME_TO_FOLLOW);
        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void testUnfollowUser() {
        ResponseEntity<Void> response = userController.unfollowUser(mockUser, USERNAME_TO_FOLLOW);

        verify(userService, times(1)).unfollowUser(mockUser, USERNAME_TO_FOLLOW);
        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void testDeactivateUser_whenUserActive() {
        mockUser.setInactive(false); // Usuário ativo
        ResponseEntity<Void> response = userController.deactivate(mockUser);

        verify(userService, times(1)).deactivateUser(mockUser);
        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void testActivateUser_whenUserInactive() {
        mockUser.setInactive(true); // Usuário inativo
        ResponseEntity<Void> response = userController.activate(mockUser);

        verify(userService, times(1)).activateUser(mockUser);
        assertEquals(204, response.getStatusCodeValue());
    }
}
