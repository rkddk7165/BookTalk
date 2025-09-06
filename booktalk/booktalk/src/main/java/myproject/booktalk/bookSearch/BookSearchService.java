package myproject.booktalk.bookSearch;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import myproject.booktalk.bookSearch.dto.BookDto;
import myproject.booktalk.bookSearch.dto.KakaoBookResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class BookSearchService {

    @Value("${kakao.api-key}")
    private String apiKey;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper om = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @PostConstruct
    void ensureApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("kakao.api-key is missing");
        }
        apiKey = apiKey.strip().replaceAll("\\p{Cntrl}", "");
    }

    /** 컨트롤러에서 호출하는 기존 시그니처 */
    public List<BookDto> searchBooks(String query) {
        if (query == null || query.isBlank()) return List.of();
        final String q = query.strip();

        // 1) 원문 전체 검색
        var r = request(q, null, 10);
        if (!r.isEmpty()) return r;

        // 2) 원문 제목 검색
        r = request(q, "title", 10);
        if (!r.isEmpty()) return r;

        // 3) 공백 제거 전체/제목 검색
        final String noSpace = q.replaceAll("\\s+", "");
        if (!noSpace.equals(q)) {
            r = request(noSpace, null, 10);
            if (!r.isEmpty()) return r;
            r = request(noSpace, "title", 10);
            if (!r.isEmpty()) return r;
        }

        // 4) ISBN처럼 보이면 ISBN 검색
        final String digits = q.replaceAll("[^0-9Xx]", "");
        if (digits.length() >= 10) {
            r = request(digits, "isbn", 10);
            if (!r.isEmpty()) return r;
        }

        return List.of();
    }

    /** Kakao Book API 호출: UTF-8 인코딩 + Authorization 헤더만 사용 */
    private List<BookDto> request(String query, @Nullable String target, int size) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://dapi.kakao.com/v3/search/book?query=" + encoded + "&size=" + size
                    + (target != null ? "&target=" + target : "");

            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .header("Authorization", "KakaoAK " + apiKey)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (res.statusCode() < 200 || res.statusCode() >= 300 || res.body() == null || res.body().isEmpty()) {
                return List.of();
            }

            KakaoBookResponse body = om.readValue(res.body(), KakaoBookResponse.class);
            if (body.getDocuments() == null || body.getDocuments().isEmpty()) {
                return List.of();
            }
            return body.getDocuments().stream().map(BookDto::from).toList();

        } catch (Exception ignored) {
            return List.of();
        }
    }
}
