package apicela.notstagram.models.dtos;

public record UserDTO(String username, String description, String profilePhoto, String verified, boolean publicProfile, int followers, int following) {
}
