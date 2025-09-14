package myproject.booktalk.board.service;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.board.Board;
import myproject.booktalk.board.BoardRepository;
import myproject.booktalk.board.BoardType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface BoardService {

    /* ===== 책 1:1(BOOK_DISCUSSION) ===== */

    /** 책 기반 게시판 생성 (중복 시 예외). title/description이 null이면 책 메타로 기본 채움 */
    Long createBookDiscussion(Long bookId, Long operatorUserId, String title, String description);

    /** 책 기반 게시판 조회 */
    Optional<Board> getBookDiscussionByBookId(Long bookId);

    boolean existsBookDiscussionByBookId(Long bookId);


    /* ===== 고정 게시판(FIXED) ===== */

    /** 고정 게시판을 보장(없으면 생성, 있으면 반환) — 서버 기동 시 호출 */
    Long ensureFixedBoard(String title, String description, Long operatorUserId);

    /** 고정 게시판 단건 조회 */
    Optional<Board> getFixedBoardByTitle(String title);

    /** 모든 고정 게시판 나열 */
    List<Board> listFixedBoards();


    /* ===== 공통 유틸 ===== */

    /** 게시판 메타 수정 */
    void updateMeta(Long boardId, String newTitle, String newDescription, Long operatorUserId);

    public String getFixedSlug(Long boardId);

    public BoardType getBoardType(Long boardId);


}
