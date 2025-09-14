package apicela.notstagram.services;

import apicela.notstagram.exceptions.UsernameAlreadyInUseException;
import apicela.notstagram.mappers.UserMapper;
import apicela.notstagram.models.dtos.FileDTO;
import apicela.notstagram.models.dtos.PostDTO;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.EditProfileRequest;
import apicela.notstagram.models.responses.UserProfileResponse;
import apicela.notstagram.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;
    private final PostService postService;

    public UserService(UserRepository userRepository, UserMapper userMapper, FileStorageService fileStorageService, PostService postService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.fileStorageService = fileStorageService;
        this.postService = postService;
    }

    public UserProfileResponse getProfile(User requester, String username) throws UsernameNotFoundException, BadRequestException {
        User target = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        validateUserVisibility(requester, target);
        List<PostDTO> targetPosts = postService.getPosts(requester, target);
        requester = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return userMapper.toUserProfileResponse(requester, targetPosts);
    }

    @Transactional
    public void followUser(User source, String targetUsername) {
        source = userRepository.findById(source.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found "));
        User target = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + targetUsername));
        target.getFollowers().add(source);
        source.getFollowing().add(target);
        userRepository.save(target);
    }

    @Transactional
    public void unfollowUser(User source, String targetUsername) {
        source = userRepository.findById(source.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        User target = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        target.getFollowers().remove(source);
        source.getFollowing().remove(target);
        userRepository.save(target);
    }

    @Transactional
    public void editProfile(User user, EditProfileRequest editProfileRequest, MultipartFile file) throws IOException {
        if (existsByUsername(editProfileRequest.username()))
            throw new UsernameAlreadyInUseException("Username already in use");

        user = userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (file != null) {
            String contentType = file.getContentType();

            if (contentType != null && contentType.startsWith("image/"))
                throw new BadRequestException("Invalid content type");

            FileDTO fileDTO = fileStorageService.saveFile(file);

            userRepository.save(userMapper.editProfile(user, editProfileRequest, fileDTO.path()));
        } else
            userRepository.save(userMapper.editProfile(user, editProfileRequest, null));

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
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public List<UUID> findFollowingList(UUID userId) {
        return userRepository.findFollowingIds(userId);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    private void validateUserVisibility(User requester, User target) throws BadRequestException {
        boolean isOwner = requester.getId().equals(target.getId());
        if (isOwner) return;

        boolean ownerInactive = target.isInactive();
        boolean ownerPrivate = !target.isPublicProfile();
        boolean requesterIsFollower = target.getFollowers().contains(requester);

        if (ownerInactive || (ownerPrivate && !requesterIsFollower)) {
            throw new BadRequestException("User is not visible");
        }
    }
}
