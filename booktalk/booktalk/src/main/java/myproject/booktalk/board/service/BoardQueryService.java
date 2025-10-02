package myproject.booktalk.board.service;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.board.dto.ActiveBoardItem;
import myproject.booktalk.board.repository.BoardRepository;
import myproject.booktalk.board.dto.BoardSearch;
import myproject.booktalk.board.BoardType;
import myproject.booktalk.board.dto.BookDiscussionBoardItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BoardQueryService {

    private final BoardRepository boardRepository;

    public List<BookDiscussionBoardItem> listBookDiscussionsSortedKo() {
        List<BookDiscussionBoardItem> list =
                boardRepository.findAllBookDiscussions(BoardType.BOOK_DISCUSSION);

        Collator collator = Collator.getInstance(Locale.KOREAN);
        list.sort(Comparator.comparing(BookDiscussionBoardItem::getTitle, collator));
        return list;
    }

    public Page<BookDiscussionBoardItem> searchBookDiscussion(BoardSearch search, Pageable pageable) {
        return boardRepository.searchBookDiscussion(search, pageable);
    }

    //최근 생성된 게시판 목록
    public List<BookDiscussionBoardItem> recentBoards(int limit) {
        return boardRepository.findRecentBookDiscussion(limit);
    }

    //가장 핫한 게시판 목록
    public List<ActiveBoardItem> hotBoards(int limit) {
        return boardRepository.findHotBookDiscussion(limit);
    }

}