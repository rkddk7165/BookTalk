package myproject.booktalk.board;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myproject.booktalk.BoardCreationRequest.BoardCreationRequestService;
import myproject.booktalk.BoardCreationRequest.BoardCreationRequestServiceImpl;
import myproject.booktalk.board.service.BoardService;
import myproject.booktalk.book.Book;
import myproject.booktalk.book.dto.ExternalBookPayload;
import myproject.booktalk.book.service.BookService;
import myproject.booktalk.bookSearch.BookSearchService;
import myproject.booktalk.bookSearch.dto.BookDto;
import myproject.booktalk.post.PostService;
import myproject.booktalk.post.dto.PostRow;
import myproject.booktalk.user.User;
import myproject.booktalk.user.session.SessionConst;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/boards")
@Slf4j
public class BoardController {

    private final BoardService boardService;
    private final BookService bookService;
    private final BoardCreationRequestService requestService;
    private final BookSearchService bookSearchService;
    private final PostService postService;

    /** 로그인 성공 후 진입: 고정 게시판 3종 + 책 검색 버튼 있는 홈 */
    @GetMapping
    public String home(Model model) {
        model.addAttribute("freeUrl", "/boards/free");
        model.addAttribute("recommendUrl", "/boards/recommend");
        model.addAttribute("quotesUrl", "/boards/quotes");
        model.addAttribute("bookSearchUrl", "/books/search");
        return "board/home";
    }

    @GetMapping("/request")
    public String requestBoardViaQuery(@RequestParam("isbn") String rawIsbn,
                                       @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser,
                                       RedirectAttributes ra) {
        // 0) 로그인 필수
        if (loginUser == null) {
            // 로그인 후 돌아올 수 있게 리다이렉트 파라미터 붙이기(선택)
            ra.addFlashAttribute("error", "게시판 생성 요청은 로그인 후 가능합니다.");
            return "redirect:/login?redirect=/boards/request?isbn=" + urlEncode(rawIsbn);
        }

        // 1) ISBN 정규화 (뒤 13자리 처리 포함)
        String isbn13 = bookService.normalizeIsbn(rawIsbn);

        // 2) 검색 서비스로 메타 보강 (가능한 경우)
        ExternalBookPayload payload = lookupPayload(isbn13);

        // 3) Book upsert (최소정보 또는 메타 포함)
        Book book = bookService.ensureBook(isbn13, payload);

        // 4) 이미 게시판이 있으면 바로 이동
        if (boardService.existsBookDiscussionByBookId(book.getId())) {
            return "redirect:/boards/book-discussion/" + book.getId();
        }

        // 5) 생성요청 등록 (여기서 null 유저 금지!)
        try {
            requestService.createRequest(book.getId(), loginUser.getId(), "사용자 요청");
        } catch (RuntimeException e) {
            // DuplicateRequestException 등 처리
            // 이미 PENDING 요청이 있으면 그냥 상세 페이지로 알림
            ra.addFlashAttribute("info", "이미 생성 요청이 접수되어 있습니다.");
        }

        // 6) 책 상세 페이지로 안내(+요청 플래그)
        return "redirect:/books/" + isbn13 + "?requested=1";
    }

    /**
     * 고정 게시판 라우팅 (free|recommend|quotes 만 허용)
     * 예: /boards/free?tab=all&sort=latest&page=0&size=50
     */
    /** 고정 게시판 라우팅 */
    @GetMapping("/{slug:free|recommend|quotes}")
    public String fixedBoard(@PathVariable String slug,
                             @RequestParam(defaultValue = "all") String tab,       // all | best | notice
                             @RequestParam(defaultValue = "latest") String sort,   // latest | popular
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size,
                             Model model) {

        // 1) 슬러그 매핑
        FixedBoardSlug fb = FixedBoardSlug.from(slug);

        // 2) 고정 게시판 보장 (slug 기반 버전 사용!)
        Long boardId = boardService.ensureFixedBoard(fb.getTitle(), fb.getDescription(), null);

        log.info("tab = {}", tab);

        // 3) 목록/공지 조회
        Page<PostRow> posts   = postService.listByBoard(boardId, tab, sort, page, size);
        List<PostRow> notices = postService.notices(boardId);

        // 4) 뷰 모델
        model.addAttribute("slug", fb.getSlug());
        model.addAttribute("boardId", boardId);
        model.addAttribute("boardTitle", fb.getTitle());
        model.addAttribute("boardDesc", fb.getDescription());
        model.addAttribute("boardType", BoardType.FIXED);

        model.addAttribute("posts", posts.getContent());    //TODO: DTO로 변환해서 뷰로 넘기기 (Map)
        model.addAttribute("page", posts);
        model.addAttribute("notices", notices);

        model.addAttribute("tab", tab);
        model.addAttribute("sort", sort);

        return "board/" + slug; // free.html / recommend.html / quotes.html
    }

    /** (선택) 책 1:1 게시판 라우팅: 승인되어 생성된 뒤 접근 */
    @GetMapping("/book-discussion/{bookId}")
    public String bookDiscussion(@PathVariable Long bookId, Model model) {
        var boardOpt = boardService.getBookDiscussionByBookId(bookId);
        if (boardOpt.isEmpty()) {
            // 아직 미승인/미생성 상태
            model.addAttribute("message", "해당 책의 게시판이 아직 생성되지 않았습니다.");
            return "redirect:/books/" + bookId; // 또는 에러 페이지
        }
        var board = boardOpt.get();

        // 책 메타(타이틀 뷰 라벨용)
        Optional<Book> bookOpt = Optional.ofNullable(board.getBook());
        String bookTitle = bookOpt.map(Book::getTitle).orElse("책 게시판");

        model.addAttribute("slug", "book");              // 뷰에서 book용 라벨 처리
        model.addAttribute("boardId", board.getId());
        model.addAttribute("boardTitle", bookTitle);
        model.addAttribute("boardDesc", "이 책에 대한 토론 게시판입니다.");
        model.addAttribute("boardType", BoardType.BOOK_DISCUSSION);
        return "board/list";
    }

    /* ===== 유틸 ===== */

    private ExternalBookPayload lookupPayload(String isbn13) {
        try {
            Optional<BookDto> dtoOpt = bookSearchService.findByIsbn13(isbn13); // 당신의 서비스에 맞춰 메서드명 조정
            if (dtoOpt.isEmpty()) return null;
            BookDto d = dtoOpt.get();
            return new ExternalBookPayload(
                    isbn13,
                    d.getTitle(),
                    d.getAuthor(), // DTO 구조에 맞춰 매핑
                    d.getPublisher(),
                    d.getPages(),
                    d.getThumbnail(),
                    null,
                    null   // LocalDate로 변환 가능하면
            );
        } catch (Exception ignore) {
            return null; // 실패해도 최소정보로 ensureBook 진행
        }
    }

    private String urlEncode(String s) {
        try { return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8); }
        catch (Exception e) { return s; }
    }
}
