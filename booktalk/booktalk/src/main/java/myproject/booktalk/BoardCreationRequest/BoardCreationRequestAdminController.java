package myproject.booktalk.BoardCreationRequest;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/board-requests")
@RequiredArgsConstructor
public class BoardCreationRequestAdminController {

    private final BoardCreationRequestService requestService;

    @GetMapping
    public String list(
            @SessionAttribute(name = "loginUser", required = true) User loginUser,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, 20);
        model.addAttribute("requests", requestService.getPendingRequests(pageable));
        return "admin/board-requests";
    }

    @PostMapping("/{id}/approve")
    public String approve(
            @PathVariable Long id,
            @SessionAttribute(name = "loginUser", required = true) User loginUser,
            RedirectAttributes ra
    ) {
        try {
            Long boardId = requestService.approve(id, loginUser.getId());
            ra.addFlashAttribute("toast", "게시판 요청이 승인되어 게시판이 생성되었습니다.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("toast", e.getMessage());
        }
        return "redirect:/admin/board-requests";
    }

    @PostMapping("/{id}/reject")
    public String reject(
            @PathVariable Long id,
            @RequestParam String reason,
            @SessionAttribute(name = "loginUser", required = true) User loginUser,
            RedirectAttributes ra
    ) {
        try {
            requestService.reject(id, loginUser.getId(), reason);
            ra.addFlashAttribute("toast", "요청이 반려되었습니다.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("toast", e.getMessage());
        }
        return "redirect:/admin/board-requests";
    }
}
