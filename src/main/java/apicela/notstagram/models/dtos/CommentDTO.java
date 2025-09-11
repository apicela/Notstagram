package apicela.notstagram.models.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentDTO(UUID id, String text, LocalDateTime createdAt, UserSummaryDTO user) {
}
