package myproject.booktalk.comment.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommentDto(
        Long id,
        Long userId,
        String authorNickname,
        String content,
        boolean deleted,
        LocalDateTime createdAt,
        List<CommentDto> replies
) {}