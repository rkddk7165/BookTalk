package myproject.booktalk.post.dto;

import java.time.LocalDateTime;

public record PostRow(
        Long id,
        String title,
        String authorNickname,
        LocalDateTime createdAt,
        Integer viewCount,
        Integer likeCount,
        Integer commentCount,
        boolean isNotice,
        boolean isBest,

        Long number   // ← 가상 번호 (nullable)
) {}