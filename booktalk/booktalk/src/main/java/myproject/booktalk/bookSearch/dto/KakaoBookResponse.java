package myproject.booktalk.bookSearch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoBookResponse {
    private List<KakaoBookDocument> documents;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoBookDocument {
        private String title;
        private List<String> authors;
        private String publisher;
        private String thumbnail;
        private String isbn;

    }
}