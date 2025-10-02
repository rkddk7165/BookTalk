package myproject.booktalk.board.repository;

import myproject.booktalk.board.dto.ActiveBoardItem;
import myproject.booktalk.board.dto.BoardSearch;
import myproject.booktalk.board.dto.BookDiscussionBoardItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BoardRepositoryCustom {

    Page<BookDiscussionBoardItem> searchBookDiscussion(BoardSearch cond, Pageable pageable);

    List<BookDiscussionBoardItem> findRecentBookDiscussion(int limit);

    List<ActiveBoardItem> findHotBookDiscussion(int limit);
}
