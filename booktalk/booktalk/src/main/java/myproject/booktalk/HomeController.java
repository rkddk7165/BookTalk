package myproject.booktalk;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import myproject.booktalk.board.FixedBoardSlug;
import myproject.booktalk.board.service.BoardService;
import myproject.booktalk.post.PostService;
import myproject.booktalk.user.Role;
import myproject.booktalk.user.User;
import myproject.booktalk.user.session.SessionConst;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PostService postService;
    private final BoardService boardService;

    @GetMapping("/")
    public String home(
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser,
            Model model
    ) {
        model.addAttribute("loginUser", loginUser);

        // 미리 만든 3개 게시판
        List<BoardEntry> boards = List.of(
                new BoardEntry("free", "자유게시판", "/boards/free"),
                new BoardEntry("recommend", "책추천게시판", "/boards/recommend"),
                new BoardEntry("quotes", "한줄글귀게시판", "/boards/quotes")
        );
        // 도서 게시판: 검색 진입 전용
        BoardEntry bookBoard = new BoardEntry("book", "도서 게시판", "/book/search");

        boolean isAdmin = (loginUser != null && loginUser.getRole() == Role.ADMIN);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("boards", boards);
        model.addAttribute("bookBoard", bookBoard);

        // 자유게시판 ID 확보
        Long freeId = boardService.ensureFixedBoard(
                FixedBoardSlug.FREE.getTitle(),
                FixedBoardSlug.FREE.getDescription(),
                null
        );
        // 최근 10개 글
        model.addAttribute("recentFree",
                postService.listByBoard(freeId, "all", "latest", 0, 10).getContent());

        // 책추천게시판
        Long recommendId = boardService.ensureFixedBoard(
                FixedBoardSlug.RECOMMEND.getTitle(),
                FixedBoardSlug.RECOMMEND.getDescription(),
                null
        );
        model.addAttribute("recentRecommend",
                postService.listByBoard(recommendId, "all", "latest", 0, 10).getContent());

        // 한줄글귀게시판
        Long quotesId = boardService.ensureFixedBoard(
                FixedBoardSlug.QUOTES.getTitle(),
                FixedBoardSlug.QUOTES.getDescription(),
                null
        );
        model.addAttribute("recentQuotes",
                postService.listByBoard(quotesId, "all", "latest", 0, 10).getContent());

        model.addAttribute("topWriters", postService.findTopWriters(5));

        return "index"; // templates/index.html
    }

    @Getter
    @AllArgsConstructor
    public static class BoardEntry {
        private String key;
        private String title;
        private String href;
    }
}
