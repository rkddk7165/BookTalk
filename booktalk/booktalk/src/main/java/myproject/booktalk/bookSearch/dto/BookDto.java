package myproject.booktalk.bookSearch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookDto {
    private String title;
    private String author;
    private String publisher;
    private String thumbnail;
    private String isbn;

    public static BookDto from(KakaoBookResponse.KakaoBookDocument doc) {
        return new BookDto(
                doc.getTitle(),
                String.join(", ", doc.getAuthors()),
                doc.getPublisher(),
                doc.getThumbnail(),
                doc.getIsbn()
        );
    }
}