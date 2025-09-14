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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    private Integer viewCount = 0;
    private Integer likeCount = 0;
    private Integer commentCount = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    /** 공지 여부 */
    private boolean isNotice = false;

    /** 개념글(베스트) 여부 */
    private boolean isBest = false;
}
