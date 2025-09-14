package apicela.notstagram.models.requests;

public record EditProfileRequest(Boolean publicProfile, String username, String description) {
}
