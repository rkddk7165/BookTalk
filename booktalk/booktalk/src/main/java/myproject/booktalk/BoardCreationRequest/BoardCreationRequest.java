package myproject.booktalk.BoardCreationRequest;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import myproject.booktalk.book.Book;
import myproject.booktalk.user.User;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class BoardCreationRequest {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Lob
    private String rejectReason;

    private LocalDateTime requestTime;

    private LocalDateTime acceptedTime;
}
