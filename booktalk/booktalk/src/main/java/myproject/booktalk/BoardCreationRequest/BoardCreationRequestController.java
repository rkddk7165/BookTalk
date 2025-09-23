package myproject.booktalk.BoardCreationRequest;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/board-requests")
@RequiredArgsConstructor
public class BoardCreationRequestController {

    private final BoardCreationRequestService requestService;

    // 요청 생성
    @PostMapping("/create")
    public String create(
            @RequestParam Long bookId,
            @SessionAttribute(name = "loginUser", required = true) User loginUser,
            RedirectAttributes ra
    ) {
        try {
            Long requestId = requestService.createRequest(bookId, loginUser.getId(), null);
            ra.addFlashAttribute("toast", "게시판 생성 요청이 등록되었습니다.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("toast", e.getMessage());
        }
        // 도서 상세 페이지로 리다이렉트 (경로는 프로젝트 상황에 맞게 수정)
        return "redirect:/book/" + bookId;
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
