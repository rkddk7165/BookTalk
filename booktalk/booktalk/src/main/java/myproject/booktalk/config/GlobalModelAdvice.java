package myproject.booktalk.config;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.board.dto.ActiveBoardItem;
import myproject.booktalk.board.dto.BookDiscussionBoardItem;
import myproject.booktalk.board.service.BoardQueryService;
import myproject.booktalk.post.PostService;
import myproject.booktalk.user.dto.TopWriterRow;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final BoardQueryService boardQueryService;
    private final PostService postService;

    @ModelAttribute("recentBoards")
    public List<BookDiscussionBoardItem> recentBoards() {
        return boardQueryService.recentBoards(5);
    }

    @ModelAttribute("hotBoards")
    public List<ActiveBoardItem> hotBoards() {
        return boardQueryService.hotBoards(3);
    }

    @ModelAttribute("topWriters")
    public List<TopWriterRow> topWriters() {
        return postService.findTopWriters(5);
    }
}
