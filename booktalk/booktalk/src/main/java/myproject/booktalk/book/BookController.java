package myproject.booktalk.book;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.BoardCreationRequest.BoardCreationRequestService;
import myproject.booktalk.board.service.BoardService;
import myproject.booktalk.book.dto.ExternalBookPayload;
import myproject.booktalk.book.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final BoardService boardService;
    private final BoardCreationRequestService requestService; // 이미 구현한 서비스 사용

    /** 책 상세: 게시판 존재 여부 표기 */
    @GetMapping("/{isbn}")
    public String detail(@PathVariable String isbn, Model model) {
        String isbn13 = bookService.normalizeIsbn(isbn);
        Optional<Book> bookOpt = bookService.findByIsbn(isbn13);
        boolean boardExists = bookOpt.isPresent()
                && boardService.existsBookDiscussionByBookId(bookOpt.get().getId());

        model.addAttribute("book", bookOpt.map(BookView::of)
                .orElse(new BookView(isbn13, null, null, null, null)));
        model.addAttribute("boardExists", boardExists);
        return "book/detail";
    }

    /** 게시판 이동(존재 시) */
    @PostMapping("/{isbn}/go-board")
    public String goBoard(@PathVariable String isbn) {
        String isbn13 = bookService.normalizeIsbn(isbn);
        Book book = bookService.findByIsbn(isbn13)
                .orElseThrow(() -> new IllegalArgumentException("책이 등록되어 있지 않습니다. ISBN=" + isbn));
        return "redirect:/boards/book-discussion/" + book.getId();
    }

    /** 게시판 생성 요청(미존재 시) */
    @PostMapping("/{isbn}/request-board")
    public String requestBoard(@PathVariable String isbn,
                               @RequestParam(required = false) String title,
                               @RequestParam(required = false) String author,
                               @RequestParam(required = false) String publisher,
                               @RequestParam(required = false) Integer pages,
                               @RequestParam(required = false) String thumbnail,
                               @RequestParam(required = false) String description) {
        String isbn13 = bookService.normalizeIsbn(isbn);
        ExternalBookPayload payload = new ExternalBookPayload(
                isbn13, title, author, publisher, pages, thumbnail, description, null
        );
        Book book = bookService.ensureBook(isbn13, payload);

        Long requesterId = /* TODO: 로그인 유저 ID */ null;
        requestService.createRequest(book.getId(), requesterId, "사용자 요청");
        return "redirect:/books/" + isbn13 + "?requested=1";
    }

    /* 뷰 전용 record */
    public record BookView(String isbn, String title, String author, String publisher, Integer pages) {
        static BookView of(Book b) {
            return new BookView(b.getIsbn(), b.getTitle(), b.getAuthor(), b.getPublisher(), b.getPages());
        }
    }
}
