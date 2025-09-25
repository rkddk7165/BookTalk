package myproject.booktalk.BoardCreationRequest;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.board.service.BoardService;
import myproject.booktalk.book.Book;
import myproject.booktalk.book.dto.ExternalBookPayload;
import myproject.booktalk.book.service.BookService;
import myproject.booktalk.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/board-requests")
@RequiredArgsConstructor
public class BoardCreationRequestController {

    private final BoardCreationRequestService requestService;
    private final BookService bookService;
    private final BoardService boardService;

    // (기존) bookId 기반 생성은 유지해도 되고, 이제는 ISBN 기반이 메인 흐름
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createWithBookId(
            @RequestParam Long bookId,
            @SessionAttribute(name = "loginUser", required = false) User loginUser
    ) {
        if (loginUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        requestService.createRequest(bookId, loginUser.getId(), null);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/create-by-isbn")
    @ResponseBody
    public ResponseEntity<?> createByIsbn(
            @RequestParam String isbn,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) Integer pages,
            @RequestParam(required = false) String thumbnail,
            @RequestParam(required = false) String description,
            @SessionAttribute(name = "loginUser", required = false) User loginUser
    ) {
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","로그인이 필요합니다."));
        }

        String isbn13 = bookService.normalizeIsbn(isbn);
        ExternalBookPayload payload = new ExternalBookPayload(
                isbn13, title, author, publisher, pages, thumbnail, description, null
        );
        Book book = bookService.ensureBook(isbn13, payload);

        // ✅ 보드 존재 여부 선확인
        var existingBoardId = boardService.findBookDiscussionBoardIdByBookId(book.getId());
        if (existingBoardId.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "이미 해당 책 게시판이 존재합니다.",
                    "boardId", existingBoardId.get()
            ));
        }

        requestService.createRequest(book.getId(), loginUser.getId(), null);
        return ResponseEntity.ok(Map.of("message","OK"));
    }

    // (선택) 예외 메시지를 깔끔하게 프론트로
    @ExceptionHandler({
            BoardCreationRequestServiceImpl.DuplicateRequestException.class,
            BoardCreationRequestServiceImpl.InvalidRequestStateException.class
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(RuntimeException e) {
        return e.getMessage();
    }

    // 내 요청 목록
    @GetMapping("/my")
    public String myRequests(
            @SessionAttribute(name = "loginUser", required = true) User loginUser,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, 10);
        model.addAttribute("requests", requestService.getMyRequests(loginUser.getId(), pageable));
        return "boardrequest/my_requests";
    }

    // 요청 취소
    @PostMapping("/{id}/cancel")
    public String cancel(
            @PathVariable Long id,
            @SessionAttribute(name = "loginUser", required = true) User loginUser,
            RedirectAttributes ra
    ) {
        try {
            requestService.cancel(id, loginUser.getId());
            ra.addFlashAttribute("toast", "요청이 취소되었습니다.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("toast", e.getMessage());
        }
        return "redirect:/board-requests/my";
    }
}
