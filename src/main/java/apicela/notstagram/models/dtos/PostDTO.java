package apicela.notstagram.models.dtos;

import apicela.notstagram.models.PostType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostDTO(UUID id,
                      String description,
                      String mediaUrl,
                      PostType type,
                      String contentType,
                      LocalDateTime createdAt,
                      UserSummaryDTO user,
                      int likesCount,
                      boolean likedByMe,
                      int commentsCount,
                      List<CommentDTO> recentComments) {
}
