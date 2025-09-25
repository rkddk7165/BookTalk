package myproject.booktalk.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myproject.booktalk.comment.Comment;
import myproject.booktalk.comment.CommentRepository;
import myproject.booktalk.comment.dto.CommentDto;
import myproject.booktalk.post.Post;
import myproject.booktalk.post.PostRepository;
import myproject.booktalk.user.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    public Long addComment(Long postId, Long userId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EmptyResultDataAccessException("게시글 없음", 1));
        Comment c = new Comment();
        c.setPost(post);
        c.setUser(new User(){ { setId(userId);} }); // 영속화는 JPA가 처리
        c.setContent(content);
        c.setDepth(0);
        Comment saved = commentRepository.save(c);

        // Post.commentCount 증가(댓글/대댓글 모두 포함하려면 여기서 +1)
        postRepository.bumpCommentCount(postId, 1); // 기존 카운터 메서드명에 맞춰 사용
        return saved.getId();
    }

    @Override
    public Long addReply(Long parentCommentId, Long userId, String content) {
        Comment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new EmptyResultDataAccessException("부모 댓글 없음", 1));
        if (parent.getParent() != null || parent.getDepth() != 0) {
            throw new IllegalStateException("대댓글은 댓글(깊이 0)에만 달 수 있습니다.");
        }
        Comment r = new Comment();
        r.setPost(parent.getPost());
        r.setUser(new User(){ { setId(userId);} });
        r.setParent(parent);
        r.setContent(content);
        r.setDepth(1);
        Comment saved = commentRepository.save(r);

        postRepository.bumpCommentCount(parent.getPost().getId(), 1);
        return parent.getPost().getId();
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new EmptyResultDataAccessException("댓글 없음", 1));
        if (!c.getUser().getId().equals(userId)) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }
        // 소프트삭제: 내용만 가리고 카운트는 유지(대댓글 보존)
        c.setDeleted(true);
        c.setContent("[삭제된 댓글입니다]");
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(Long postId) {
        // 루트 댓글들
        List<Comment> roots = commentRepository.findRoots(postId);
        log.info("Service: comments = {}", roots);

        // 자식들을 묶어 DTO로 변환
        return roots.stream().map(root -> new CommentDto(
                root.getId(), root.getUser().getId(), root.getUser().getNickname(), root.getContent(),
                root.isDeleted(), root.getCreatedAt(),
                commentRepository.findChildren(root.getId()).stream().map(ch -> new CommentDto(
                        ch.getId(), ch.getUser().getId(), ch.getUser().getNickname(), ch.getContent(),
                        ch.isDeleted(), ch.getCreatedAt(), List.of()
                )).toList()
        )).toList();
    }

    @Override
    public long countByUserId(Long userId) {
        return commentRepository.countByUser_Id(userId);
    }

    @Override
    public List<Comment> findRecentByUserId(Long userId, Pageable pageable) {
        return commentRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);
    }

}
