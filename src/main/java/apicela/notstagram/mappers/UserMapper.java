package apicela.notstagram.mappers;

import apicela.notstagram.models.dtos.PostDTO;
import apicela.notstagram.models.dtos.UserDTO;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.EditProfileRequest;
import apicela.notstagram.models.responses.UserProfileResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {
    public User editProfile(User user, EditProfileRequest editProfileRequest, String profilePhoto) {
        user.setPublicProfile(editProfileRequest.publicProfile() != null ? editProfileRequest.publicProfile() : user.isPublicProfile());
        user.setUsername(editProfileRequest.username() != null ? editProfileRequest.username() : user.getUsername());
        user.setDescription(editProfileRequest.description() != null ? editProfileRequest.description() : user.getDescription());
        user.setProfilePhoto(profilePhoto != null ? profilePhoto : user.getProfilePhoto());
        return user;
    }

    public UserDTO userToUserDTO(User user) {
        return new UserDTO(
                user.getUsername(),
                user.getDescription(),
                user.getProfilePhoto(),
                user.isVerified(),
                user.isPublicProfile(),
                user.getFollowers().size(),
                user.getFollowing().size()
        );
    }

    public UserProfileResponse toUserProfileResponse(User user, List<PostDTO> posts) {
        return new UserProfileResponse(userToUserDTO(user), posts);
    }

}
