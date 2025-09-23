package myproject.booktalk.board.service;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.board.BoardRepository;
import myproject.booktalk.board.BoardType;
import myproject.booktalk.board.dto.BookDiscussionBoardItem;
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
}