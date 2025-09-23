package myproject.booktalk.board;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {


    // Book 기준 조회/존재여부
    Optional<Board> findByBook_Id(Long bookId);
    boolean existsByBook_Id(Long bookId);

    // 고정 게시판용 (boardType=FIXED && title으로 구분)
    Optional<Board> findByBoardTypeAndTitle(BoardType boardType, String title);
    boolean existsByBoardTypeAndTitle(BoardType boardType, String title);
    List<Board> findAllByBoardType(BoardType boardType);

    // Book.isbn 기준 조회/존재여부 (연관 경로 사용)
    Optional<Board> findByBook_Isbn(String isbn13);
    boolean existsByBook_Isbn(String isbn13);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Board b where b.id = :id")
    Optional<Board> findForUpdate(@Param("id") Long id);

    Optional<Board> findByTitle(String title);

    Optional<Board> findByBookIdAndBoardType(Long bookId, BoardType boardType);

    default boolean existsBookDiscussionForBook(Long bookId){
        return findByBookIdAndBoardType(bookId, BoardType.BOOK_DISCUSSION).isPresent();
    }

}
