package myproject.booktalk.post.dto;

public record PostCreateRequest(

        Long boardId,
        Long userId,
        String title,
        String content,
        boolean isNotice,
        boolean isBest
) { }
