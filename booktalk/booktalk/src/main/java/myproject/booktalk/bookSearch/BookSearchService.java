package myproject.booktalk.bookSearch;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import myproject.booktalk.book.Book;
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
import java.util.Optional;

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

    /** ISBN-13로 단일 도서 찾기 (Kakao target=isbn 사용) */
    public Optional<BookDto> findByIsbn13(String rawIsbn) {
        if (rawIsbn == null || rawIsbn.isBlank()) return Optional.empty();

        // "1167372867 9791167372864" 같은 입력에서도 뒤쪽 13자리만 안전 추출
        String isbn13 = tailIsbn13(rawIsbn);
        if (isbn13 == null) return Optional.empty();

        // Kakao API를 isbn 타겟으로 조회
        List<BookDto> results = request(isbn13, "isbn", 10);
        if (results.isEmpty()) return Optional.empty();

        // 결과 중에서 ISBN-13이 정확히 일치하는 첫 항목만 반환
        return results.stream()
                .filter(dto -> isbn13.equals(safeIsbn13(dto)))
                .findFirst()
                // 혹시 정확 일치가 없다면 첫 결과라도 리턴하고 싶다면 아래 주석 해제
                //.or(() -> Optional.of(results.get(0)))
                ;
    }

    /** DTO의 raw isbn에서 13자리만 안전 추출 */
    private String safeIsbn13(BookDto dto) {
        if (dto == null || dto.getIsbn() == null) return null;
        String digits = dto.getIsbn().replaceAll("[^0-9]", "");
        if (digits.length() >= 13) {
            return digits.substring(digits.length() - 13);
        }
        return null;
    }

    /** 입력 문자열에서 ISBN-13 추출 ("ISBN10 ISBN13" 형태도 처리) */
    private String tailIsbn13(String raw) {
        String digits = raw.replaceAll("[^0-9Xx]", "");
        // X가 섞인 경우도 있으나 Kakao isbn 타겟은 13자리 숫자 매칭이 안전
        // 뒤에서 13자리 숫자만 추출
        String onlyDigits = digits.replaceAll("[^0-9]", "");
        if (onlyDigits.length() >= 13) {
            return onlyDigits.substring(onlyDigits.length() - 13);
        }
        return null;
    }


}
