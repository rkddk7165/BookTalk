package myproject.booktalk.book.dto;

import java.time.LocalDate;

public record ExternalBookPayload(
        String isbn,                 // 외부에서 오는 원본 ISBN(10/13 섞여 있을 수 있음)
        String title,
        String author,
        String publisher,
        Integer pages,
        String thumbnail,            // URL 또는 base64/바이너리 텍스트(현재 @Lob 문자열)
        String description,
        LocalDate publicationDate
) {}
