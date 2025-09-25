package myproject.booktalk.comment.service;

import myproject.booktalk.comment.Comment;
import myproject.booktalk.comment.dto.CommentDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {

    Long addComment(Long postId, Long userId, String content);
    Long addReply(Long parentCommentId, Long userId, String content);
    void deleteComment(Long commentId, Long userId);
    List<CommentDto> getComments(Long postId);


    long countByUserId(Long userId);

    List<Comment> findRecentByUserId(Long userId, Pageable pageable);
}
