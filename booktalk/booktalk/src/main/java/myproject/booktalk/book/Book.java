package myproject.booktalk.book;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter @Setter
public class Book {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private Integer pages;

    @Lob
    private String thumbnail;

    @Lob
    private String description;

    private LocalDate publicationDate;

}
