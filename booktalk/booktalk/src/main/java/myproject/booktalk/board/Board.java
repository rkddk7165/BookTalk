package myproject.booktalk.board;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import myproject.booktalk.book.Book;
import myproject.booktalk.user.User;

import java.time.LocalDate;

@Entity
@Getter @Setter
public class Board {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    private BoardType boardType;

    private LocalDate createdAt;



}
