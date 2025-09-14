package myproject.booktalk.post.dto;

public record PostUpdateRequest(

        Long postId,
        Long editorUserId,
        String title,
        String content,
        Boolean isNotice, // null이면 변경 안 함
        Boolean isBest

) {
}
