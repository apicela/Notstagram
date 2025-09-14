package apicela.notstagram.mappers;

import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.EditProfileRequest;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User editProfile(User user, EditProfileRequest editProfileRequest, String profilePhoto) {
        user.setPublicProfile(editProfileRequest.publicProfile() != null ? editProfileRequest.publicProfile() : user.isPublicProfile());
        user.setUsername(editProfileRequest.username() != null ? editProfileRequest.username() : user.getUsername());
        user.setDescription(editProfileRequest.description() != null ? editProfileRequest.description() : user.getDescription());
        user.setProfilePhoto(profilePhoto != null ? profilePhoto : user.getProfilePhoto());
        return user;
    }
}
