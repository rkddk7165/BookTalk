package myproject.booktalk.post;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myproject.booktalk.board.service.BoardService;
import myproject.booktalk.board.BoardType;
import myproject.booktalk.board.FixedBoardSlug;
import myproject.booktalk.comment.CommentRepository;
import myproject.booktalk.comment.dto.CommentDto;
import myproject.booktalk.comment.service.CommentService;
import myproject.booktalk.post.dto.PostCreateRequest;
import myproject.booktalk.post.dto.PostDetailDto;
import myproject.booktalk.post.dto.PostUpdateRequest;
import myproject.booktalk.user.User;
import myproject.booktalk.user.session.SessionConst;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final BoardService boardService;
    private final CommentService commentService;

    /* -------------------- 글쓰기 폼 -------------------- */
    /**
     * 고정 게시판에서 글쓰기:
     *   /post/new?fixed=free|recommend|quotes
     * 책 게시판에서 글쓰기:
     *   /post/new?boardId=123
     */
    @GetMapping("/post/new")
    public String newForm(@RequestParam(required = false) String fixed,
                          @RequestParam(required = false) Long boardId,
                          Model model) {

        Long resolvedBoardId = resolveBoardId(fixed, boardId);
        model.addAttribute("boardId", resolvedBoardId);
        model.addAttribute("fixed", fixed); // 뷰에서 라벨/리디렉트에 사용
        return "post/form"; // 작성/수정 겸용 폼(템플릿은 프로젝트에 맞춰 구성)
    }

    /* -------------------- 글 등록 -------------------- */
    @PostMapping("/post")
    public String create(@RequestParam Long boardId,
                         @RequestParam String title,
                         @RequestParam String content,
                         @RequestParam(required = false, defaultValue = "false") boolean isNotice,
                         @RequestParam(required = false, defaultValue = "false") boolean isBest,
                         @RequestParam(required = false) String fixed, // 돌아갈 목록 경로 유지용
                         RedirectAttributes ra,
                             @SessionAttribute(name = SessionConst.LOGIN_USER) User user) {

        Long loginUserId = user.getId();
        if (title == null || title.isBlank()) {
            ra.addFlashAttribute("error", "제목을 입력해주세요.");
            return "redirect:/post/new" + redirectQuery(fixed, boardId);
        }

        Long postId = postService.create(new PostCreateRequest(
                boardId, loginUserId, title, content, isNotice, isBest
        ));
        ra.addFlashAttribute("success", "글이 등록되었습니다.");
        return "redirect:/post/" + postId;
    }

    /* -------------------- 상세(조회수 +1) -------------------- */
    @GetMapping("/post/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) String slug,
                         @SessionAttribute(name = SessionConst.LOGIN_USER) User loginUser,
                         Model model) {
        var dto = postService.getDetail(id, true);
        model.addAttribute("post", dto);

        List<CommentDto> comments = commentService.getComments(id);

        model.addAttribute("comments", comments);

        log.info(comments.toString());

        Long loginUserId = (loginUser != null) ? loginUser.getId() : null;
        model.addAttribute("loginUserId", loginUserId);

        boolean fixed = boardService.getBoardType(dto.boardId()) == BoardType.FIXED;
        model.addAttribute("isFixedBoard", fixed);
        if (fixed) {
            // slug가 파라미터로 넘어오지 않았다면 boardId 기반으로 찾아도 됨
            if (slug == null) {
                slug = boardService.getFixedSlug(dto.boardId());
            }
            model.addAttribute("slug", slug);
        }
        return "post/detail";
    }

    /* -------------------- 수정 폼 -------------------- */
    @GetMapping("/post/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PostDetailDto dto = postService.getDetail(id, false);
        model.addAttribute("post", dto);
        return "post/form";
    }

    /* -------------------- 수정 처리 -------------------- */
    @PostMapping("/post/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam String title,
                       @RequestParam String content,
                       @RequestParam(required = false) Boolean isNotice,
                       @RequestParam(required = false) Boolean isBest,
                       RedirectAttributes ra,
                       @SessionAttribute(name = SessionConst.LOGIN_USER) User user) {

        Long editorUserId = user.getId();
        postService.update(new PostUpdateRequest(id, editorUserId, title, content, isNotice, isBest));
        ra.addFlashAttribute("success", "수정되었습니다.");
        return "redirect:/post/" + id;
    }

    /* -------------------- 삭제 -------------------- */
    @PostMapping("/post/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra,
                         @SessionAttribute(name = SessionConst.LOGIN_USER) User user) {
        Long requesterUserId = user.getId();
        Long boardId = postService.getDetail(id, false).boardId(); // 목록 리다이렉트용
        postService.delete(id, requesterUserId);
        ra.addFlashAttribute("success", "삭제되었습니다.");

        // 고정/책 게시판에 따라 목록으로 이동
        if (boardService.getBoardType(boardId) == BoardType.FIXED) {
            String slug = boardService.getFixedSlug(boardId); // 필요 시 BoardService에 구현
            return "redirect:/boards/" + slug;
        } else {
            return "redirect:/boards/book-discussion/" + boardId;
        }
    }

    /* -------------------- 좋아요(+1) / 취소(-1) -------------------- */
    @PostMapping("/post/{id}/like")
    public String like(
            @PathVariable Long id,
            @SessionAttribute(name = "loginUser", required = true) User loginUser,
            RedirectAttributes ra
    ) {
        try {
            postService.like(id, loginUser.getId());
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", "좋아요는 한 번만 누를 수 있습니다.");
        }
        return "redirect:/post/" + id;
    }

    @PostMapping("/post/{id}/dislike")
    public String dislike(
            @PathVariable Long id,
            @SessionAttribute(name = "loginUser", required = true) User loginUser,
            RedirectAttributes ra
    ) {
        try {
            postService.dislike(id, loginUser.getId());
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", "싫어요는 한 번만 누를 수 있습니다.");
        }
        return "redirect:/post/" + id;
    }

    /* ==================== 헬퍼 ==================== */

    /** fixed(slug) 또는 boardId로 실제 boardId를 구한다 */
    private Long resolveBoardId(String fixed, Long boardId) {
        if (fixed != null && !fixed.isBlank()) {
            FixedBoardSlug fb = FixedBoardSlug.from(fixed);
            return boardService.ensureFixedBoard(fb.getTitle(), fb.getDescription(), null);
        }
        if (boardId == null) throw new IllegalArgumentException("boardId가 필요합니다.");
        return boardId;
    }


    /** 글쓰기 실패 시 돌아갈 쿼리스트링 */
    private String redirectQuery(String fixed, Long boardId) {
        if (fixed != null && !fixed.isBlank()) return "?fixed=" + fixed;
        return "?boardId=" + boardId;
    }
}
