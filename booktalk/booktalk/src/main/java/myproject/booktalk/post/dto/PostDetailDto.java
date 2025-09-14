package myproject.booktalk.post.dto;

import java.time.LocalDateTime;

public record PostDetailDto(

        Long id,
        Long boardId,
        Long userId,
        String authorNickname,
        String title,
        String content,
        Integer viewCount,
        Integer likeCount,
        Integer commentCount,
        boolean isNotice,
        boolean isBest,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
