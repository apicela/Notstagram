package apicela.notstagram.models.responses;

import java.util.UUID;

public record AuthResponse(String acessToken, String refreshToken, int access_expires_in) { }
