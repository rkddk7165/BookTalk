package myproject.booktalk.comment;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.comment.service.CommentService;
import myproject.booktalk.user.User;
import myproject.booktalk.user.session.SessionConst;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/post/{postId}/comments")
    public String submitComment(@PathVariable Long postId,
                                @RequestParam("content") String content,
                                @RequestParam(value = "parentId", required = false) Long parentId,
                                @SessionAttribute(name = "loginUser", required = true) User loginUser,
                                RedirectAttributes ra) {
        try {
            if (parentId == null) {
                commentService.addComment(postId, loginUser.getId(), content);
            } else {
                commentService.addReply(parentId, loginUser.getId(), content);
            }
            ra.addFlashAttribute("toast", "등록되었습니다.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/post/" + postId + "#comments";
    }

    @PostMapping("/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId,
                                @RequestParam("postId") Long postId,
                                @SessionAttribute(name = "loginUser", required = true) User loginUser,
                                RedirectAttributes ra) {
        commentService.deleteComment(commentId, loginUser.getId());
        ra.addFlashAttribute("toast", "삭제되었습니다.");
        return "redirect:/post/" + postId + "#comments";
    }



}
