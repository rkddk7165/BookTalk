package myproject.booktalk.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import myproject.booktalk.board.Board;
import myproject.booktalk.user.User;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Post {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;

    @Lob
    private String content;

    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
