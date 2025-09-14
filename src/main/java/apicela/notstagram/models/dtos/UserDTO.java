package apicela.notstagram.models.dtos;

public record UserDTO(String username, String description, String profilePhoto, boolean verified, boolean publicProfile,
                      int followers, int following) {
}
