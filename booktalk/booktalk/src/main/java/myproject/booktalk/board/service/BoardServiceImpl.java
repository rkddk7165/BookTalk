package myproject.booktalk.board.service;



import lombok.RequiredArgsConstructor;
import myproject.booktalk.board.Board;
import myproject.booktalk.board.repository.BoardRepository;
import myproject.booktalk.board.BoardType;
import myproject.booktalk.board.FixedBoardSlug;
import myproject.booktalk.book.Book;
import myproject.booktalk.book.BookRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BookRepository bookRepository;

    // 서버 실행 시 보장되는 slug ↔ boardId 매핑 캐시
    private final Map<FixedBoardSlug, Long> fixedBoardCache = new EnumMap<>(FixedBoardSlug.class);

    /* =======================
     * BOOK_DISCUSSION
     * ======================= */

    @Override
    @Transactional
    public Long createBookDiscussion(Long bookId, Long operatorUserId, String title, String description) {
        if (boardRepository.existsByBook_Id(bookId)) {
            throw new BoardAlreadyExistsException("이미 해당 책의 게시판이 존재합니다. bookId=" + bookId);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book이 존재하지 않습니다. id=" + bookId));

        Board board = new Board();
        board.setBook(book);
        board.setBoardType(BoardType.BOOK_DISCUSSION);
        board.setTitle(nonBlankOr(title, defaultTitleFromBook(book)));
        board.setDescription(Objects.requireNonNullElse(description, ""));
        board.setCreatedAt(LocalDateTime.now());
        // operatorUserId를 기록하고 싶다면 board.setUser(...) or createdBy 필드 추가 고려

        try {
            boardRepository.saveAndFlush(board);
        } catch (DataIntegrityViolationException e) {
            // 동시 승인 경합 방어
            if (boardRepository.existsByBook_Id(bookId)) {
                throw new BoardAlreadyExistsException("이미 해당 책의 게시판이 존재합니다. bookId=" + bookId);
            }
            throw e;
        }
        return board.getId();
    }

    @Override
    public Optional<Board> getBookDiscussionByBookId(Long bookId) {
        return boardRepository.findByBook_Id(bookId);
    }

    @Override
    public boolean existsBookDiscussionByBookId(Long bookId) {
        return boardRepository.existsByBook_Id(bookId);
    }


    /* =======================
     * FIXED
     * ======================= */

    @Transactional
    public Long ensureFixedBoard(String title, String description, Long operatorUserId) {
        // slug는 title로 구분하지 말고 FixedBoardSlug로 명확히 하는 게 더 안전함
        Board board = boardRepository.findByTitle(title)
                .orElseGet(() -> {
                    Board b = new Board();
                    b.setTitle(title);
                    b.setDescription(description);
                    b.setBoardType(BoardType.FIXED);
                    return boardRepository.save(b);
                });

        // 캐시에 등록
        FixedBoardSlug slug = FixedBoardSlug.fromTitle(title); // 필요 시 enum에 title → slug 변환 메서드 추가
        fixedBoardCache.put(slug, board.getId());

        return board.getId();
    }

    /** boardId로부터 slug 문자열을 구한다 */
    public String getFixedSlug(Long boardId) {
        return fixedBoardCache.entrySet().stream()
                .filter(e -> e.getValue().equals(boardId))
                .map(e -> e.getKey().getSlug())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("고정 게시판이 아님: boardId=" + boardId));
    }

    @Override
    public Optional<Board> getFixedBoardByTitle(String title) {
        return boardRepository.findByBoardTypeAndTitle(BoardType.FIXED, requiredTitle(title));
    }

    @Override
    public List<Board> listFixedBoards() {
        return boardRepository.findAllByBoardType(BoardType.FIXED);
    }

    /** boardId로부터 타입 얻기 */
    public BoardType getBoardType(Long boardId) {
        return boardRepository.findById(boardId)
                .map(Board::getBoardType)
                .orElseThrow(() -> new IllegalArgumentException("게시판이 없음: " + boardId));
    }

    @Override
    public Optional<Long> findBookDiscussionBoardIdByBookId(Long bookId) {
        return boardRepository.findIdByBoardTypeAndBookId(BoardType.BOOK_DISCUSSION, bookId);
    }


    /* =======================
     * 공통
     * ======================= */

    @Override
    @Transactional
    public void updateMeta(Long boardId, String newTitle, String newDescription, Long operatorUserId) {
        Board board = boardRepository.findForUpdate(boardId)
                .orElseThrow(() -> new BoardNotFoundException("게시판이 없습니다. id=" + boardId));

        if (notBlank(newTitle)) board.setTitle(newTitle.trim());
        if (newDescription != null) board.setDescription(newDescription);
        // updatedBy/updatedAt 필드가 없으니 필요하면 엔티티에 추가 권장
    }


    public Optional<Board> findById(Long boardId){
        return boardRepository.findById(boardId);
    }


    /* =======================
     * 유틸/예외
     * ======================= */

    private String defaultTitleFromBook(Book book) {
        if (book.getTitle() != null && !book.getTitle().isBlank()) {
            return book.getTitle().trim();
        }
        return "Book Discussion (" + book.getIsbn() + ")";
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isBlank();
    }

    private String nonBlankOr(String s, String fallback) {
        return notBlank(s) ? s.trim() : fallback;
    }

    private String requiredTitle(String title) {
        if (!notBlank(title)) {
            throw new IllegalArgumentException("고정 게시판 제목(title)은 필수입니다.");
        }
        return title.trim();
    }

    public static class BoardAlreadyExistsException extends RuntimeException {
        public BoardAlreadyExistsException(String message) { super(message); }
    }
    public static class BoardNotFoundException extends RuntimeException {
        public BoardNotFoundException(String message) { super(message); }
    }
}
