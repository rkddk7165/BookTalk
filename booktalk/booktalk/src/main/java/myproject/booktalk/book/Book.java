package myproject.booktalk.book;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter @Setter
public class Book {

    @Id @GeneratedValue
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
