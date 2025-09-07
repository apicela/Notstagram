package apicela.notstagram.models.requests;

public record CompleteRegisterRequest(String username, String password, boolean publicProfile) {
}
