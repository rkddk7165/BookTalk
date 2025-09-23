package myproject.booktalk.comment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import myproject.booktalk.post.Post;
import myproject.booktalk.user.User;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Comment {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Lob
    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean deleted = false;
    private Integer depth = 0;      // 0 = 댓글, 1 = 대댓글


}
