package apicela.notstagram.models.responses;

import apicela.notstagram.models.dtos.PostDTO;
import apicela.notstagram.models.dtos.UserDTO;

import java.util.List;

public record UserProfileResponse(UserDTO userDTO, List<PostDTO> posts) {
}
