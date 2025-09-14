package myproject.booktalk.book.service;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.book.Book;
import myproject.booktalk.book.BookRepository;
import myproject.booktalk.book.dto.ExternalBookPayload;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    /* ======================
     * 조회
     * ====================== */
    @Override
    public Optional<Book> findByIsbn(String rawIsbn) {
        String isbn13 = normalizeIsbn(rawIsbn);
        return bookRepository.findByIsbn(isbn13);
    }

    /* ======================
     * 생성/업서트
     * ====================== */
    @Override
    @Transactional
    public Book ensureBook(String rawIsbn, ExternalBookPayload payload) {
        String isbn13 = normalizeIsbn(rawIsbn);

        // 이미 있으면 보수적 병합(빈 자리만 채움)
        Optional<Book> existingOpt = bookRepository.findByIsbn(isbn13);
        if (existingOpt.isPresent()) {
            Book b = existingOpt.get();
            conservativeMerge(b, payload);
            // JPA dirty checking으로 업데이트
            return b;
        }

        // 없으면 새로 생성
        Book b = new Book();
        b.setIsbn(isbn13);
        if (payload != null) {
            if (notBlank(payload.title()))        b.setTitle(payload.title().trim());
            if (notBlank(payload.author()))       b.setAuthor(payload.author().trim());
            if (notBlank(payload.publisher()))    b.setPublisher(payload.publisher().trim());
            if (payload.pages() != null)          b.setPages(payload.pages());
            if (notBlank(payload.thumbnail()))    b.setThumbnail(payload.thumbnail());
            if (notBlank(payload.description()))  b.setDescription(payload.description());
            if (payload.publicationDate() != null) b.setPublicationDate(payload.publicationDate());
        }
        // 데이터가 거의 없더라도 최소 정보로 insert (외부 상세는 이후 refreshMeta로 갱신)
        try {
            return bookRepository.saveAndFlush(b);
        } catch (DataIntegrityViolationException e) {
            // 동시 업서트 경합 방어 (유니크 인덱스 권장)
            return bookRepository.findByIsbn(isbn13)
                    .orElseThrow(() -> e);
        }
    }

    @Override
    @Transactional
    public void refreshMeta(String rawIsbn, ExternalBookPayload payload, boolean overwriteNonNull) {
        String isbn13 = normalizeIsbn(rawIsbn);
        Book book = bookRepository.findByIsbn(isbn13)
                .orElseThrow(() -> new IllegalArgumentException("Book not found for ISBN: " + rawIsbn));

        if (payload == null) return;

        if (overwriteNonNull) {
            // 덮어쓰기 정책
            if (notBlank(payload.title()))        book.setTitle(payload.title().trim());
            if (notBlank(payload.author()))       book.setAuthor(payload.author().trim());
            if (notBlank(payload.publisher()))    book.setPublisher(payload.publisher().trim());
            if (payload.pages() != null)          book.setPages(payload.pages());
            if (notBlank(payload.thumbnail()))    book.setThumbnail(payload.thumbnail());
            if (notBlank(payload.description()))  book.setDescription(payload.description());
            if (payload.publicationDate() != null) book.setPublicationDate(payload.publicationDate());
        } else {
            // 보수적 병합(기존값이 비어있을 때만 채움)
            conservativeMerge(book, payload);
        }
    }

    /* ======================
     * 내부 유틸
     * ====================== */

    private void conservativeMerge(Book b, ExternalBookPayload p) {
        if (p == null) return;

        if (isBlank(b.getTitle())        && notBlank(p.title()))        b.setTitle(p.title().trim());
        if (isBlank(b.getAuthor())       && notBlank(p.author()))       b.setAuthor(p.author().trim());
        if (isBlank(b.getPublisher())    && notBlank(p.publisher()))    b.setPublisher(p.publisher().trim());
        if (b.getPages() == null         && p.pages() != null)          b.setPages(p.pages());
        if (isBlank(b.getThumbnail())    && notBlank(p.thumbnail()))    b.setThumbnail(p.thumbnail());
        if (isBlank(b.getDescription())  && notBlank(p.description()))  b.setDescription(p.description());
        if (b.getPublicationDate() == null && p.publicationDate() != null) b.setPublicationDate(p.publicationDate());
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isBlank();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isBlank();
    }

    /**
     * ISBN 입력(10/13/하이픈/공백 포함 가능)을 프로젝트 표준인 ISBN-13으로 정규화
     */
    @Override
    public String normalizeIsbn(String rawIsbn) {
        if (rawIsbn == null) throw new IllegalArgumentException("ISBN is null");
        String digits = rawIsbn.replaceAll("[^0-9Xx]", "").toUpperCase(Locale.ROOT);

        // 외부 응답이 "ISBN10 ISBN13" 같이 섞여오는 케이스 방어: 뒤 13자리만 추출
        if (digits.length() > 13 && digits.matches(".*\\d{13}$")) {
            digits = digits.substring(digits.length() - 13);
        }

        if (digits.length() == 13 && digits.matches("\\d{13}")) {
            return digits;
        }
        if (digits.length() == 10) {
            return toIsbn13(digits);
        }
        throw new IllegalArgumentException("Invalid ISBN: " + rawIsbn);
    }

    /** ISBN-10 → ISBN-13 변환 */
    private String toIsbn13(String isbn10) {
        // 접두사 978 + 앞 9자리
        String core = "978" + isbn10.substring(0, 9);
        int sum = 0;
        for (int i = 0; i < core.length(); i++) {
            int d = core.charAt(i) - '0';
            sum += (i % 2 == 0) ? d : d * 3;
        }
        int check = (10 - (sum % 10)) % 10;
        return core + check;
    }
}