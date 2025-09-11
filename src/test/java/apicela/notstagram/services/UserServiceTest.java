package apicela.notstagram.services;

import apicela.notstagram.models.entities.User;
import apicela.notstagram.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User sourceUser;
    private User targetUser;
    private UUID sourceId;
    private UUID targetId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sourceId = UUID.randomUUID();
        targetId = UUID.randomUUID();

        sourceUser = new User();
        sourceUser.setId(sourceId);
        sourceUser.setUsername("source");
        sourceUser.setInactive(false);

        targetUser = new User();
        targetUser.setId(targetId);
        targetUser.setUsername("target");
        targetUser.setInactive(false);
    }

    @Test
    void testFollowUser_success() {
        when(userRepository.findById(sourceId)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findByUsername("target")).thenReturn(Optional.of(targetUser));

        userService.followUser(sourceUser, "target");

        assertTrue(targetUser.getFollowers().contains(sourceUser));
        assertTrue(sourceUser.getFollowing().contains(targetUser));
        verify(userRepository, times(1)).save(targetUser);
    }

    @Test
    void testFollowUser_sourceNotFound() {
        when(userRepository.findById(sourceId)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.followUser(sourceUser, "target"));
        assertEquals("Usuário source não encontrado: ", exception.getMessage());
    }

    @Test
    void testUnfollowUser_success() {
        sourceUser.getFollowing().add(targetUser);
        targetUser.getFollowers().add(sourceUser);

        when(userRepository.findById(sourceId)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findByUsername("target")).thenReturn(Optional.of(targetUser));

        userService.unfollowUser(sourceUser, "target");

        assertFalse(targetUser.getFollowers().contains(sourceUser));
        assertFalse(sourceUser.getFollowing().contains(targetUser));
        verify(userRepository, times(1)).save(targetUser);
    }

    @Test
    void testDeactivateUser() {
        userService.deactivateUser(sourceUser);
        assertTrue(sourceUser.isInactive());
        verify(userRepository, times(1)).save(sourceUser);
    }

    @Test
    void testActivateUser() {
        sourceUser.setInactive(true);
        userService.activateUser(sourceUser);
        assertFalse(sourceUser.isInactive());
        verify(userRepository, times(1)).save(sourceUser);
    }

    @Test
    void testGetUserByEmail_found() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sourceUser));
        User result = userService.getUserByEmail("test@example.com");
        assertEquals(sourceUser, result);
    }

    @Test
    void testGetUserByEmail_notFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> userService.getUserByEmail("unknown@example.com"));
    }

    @Test
    void testFindFollowingList() {
        List<UUID> followingIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(userRepository.findFollowingIds(sourceId)).thenReturn(followingIds);

        List<UUID> result = userService.findFollowingList(sourceId);
        assertEquals(followingIds, result);
    }
}
