package apicela.notstagram.services;

import apicela.notstagram.models.entities.User;
import apicela.notstagram.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void followUser(User source, String targetUsername) {
        source = userRepository.findById(source.getId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário source não encontrado: "));
        User target = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new EntityNotFoundException("Usuário target não encontrado: " + targetUsername));
        target.getFollowers().add(source);
        source.getFollowing().add(target);
        userRepository.save(target);
    }

    @Transactional
    public void unfollowUser(User source, String targetUsername) {
        source = userRepository.findById(source.getId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        User target = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        target.getFollowers().remove(source);
        source.getFollowing().remove(target);
        userRepository.save(target);
    }

    public void deactivateUser(User user) {
        user.setInactive(true);
        userRepository.save(user);
    }

    public void activateUser(User user) {
        user.setInactive(false);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return getUserByEmail(email);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    public List<UUID> findFollowingList(UUID userId) {
        return userRepository.findFollowingIds(userId);
    }
}
