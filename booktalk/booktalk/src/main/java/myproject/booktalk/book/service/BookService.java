package myproject.booktalk.book.service;


import myproject.booktalk.book.Book;
import myproject.booktalk.book.dto.ExternalBookPayload;

import java.util.Optional;

public interface BookService {

    /** ISBN(10/13/하이픈 포함 가능)로 단건 조회 */
    Optional<Book> findByIsbn(String rawIsbn);

    /**
     * 외부 검색 결과를 기반으로 Book을 확보한다.
     * - 없으면 생성
     * - 있으면 '보수적 병합'(DB에 비어있는 필드만 payload 값으로 채움)
     */
    Book ensureBook(String rawIsbn, ExternalBookPayload payload);

    /**
     * 메타데이터 갱신.
     * @param overwriteNonNull true면 DB의 기존 값이 있어도 payload 값으로 덮어씀
     */
    void refreshMeta(String rawIsbn, ExternalBookPayload payload, boolean overwriteNonNull);

    /** 유틸: 입력 ISBN을 프로젝트 표준인 ISBN-13으로 정규화해서 반환 */
    String normalizeIsbn(String rawIsbn);
}
