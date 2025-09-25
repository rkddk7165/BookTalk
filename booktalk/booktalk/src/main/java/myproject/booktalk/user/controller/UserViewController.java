package myproject.booktalk.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import myproject.booktalk.BoardCreationRequest.BoardCreationRequestService;
import myproject.booktalk.comment.service.CommentService;
import myproject.booktalk.post.PostService;
import myproject.booktalk.user.BadCredentialException;
import myproject.booktalk.user.User;
import myproject.booktalk.user.UserService;
import myproject.booktalk.user.session.SessionConst;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserViewController {

    private final PostService postService;
    private final CommentService commentService;
    private final BoardCreationRequestService requestService;

    // 현재 로그인 세션/기기 리스트 관리 (개별 세션 만료 등)
    //private final SessionService sessionService;

    // 사용자 프로필/설정/비밀번호 변경 등
    private final UserService userService;

    /** 마이페이지 진입 */
    @GetMapping("/me")
    public String myPage(
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser,
            @RequestParam(name = "tab", required = false) String tab,
            Model model
    ) {
        if (loginUser == null) return "redirect:/login?redirect=/me";

        // 기본 사용자 정보
        model.addAttribute("user", loginUser);

        // 활동 요약
        var stats = new StatsDto(
                postService.countByUserId(loginUser.getId()),
                commentService.countByUserId(loginUser.getId()),
                requestService.countByUserId(loginUser.getId())
        );
        model.addAttribute("stats", stats);

        // 최근 활동
        model.addAttribute("recentPosts", postService.findRecentByUserId(loginUser.getId(), PageRequest.of(0, 5)));
        model.addAttribute("recentComments", commentService.findRecentByUserId(loginUser.getId(), PageRequest.of(0, 5)));

        // 내 게시판 생성 요청
        model.addAttribute("myBoardRequests", requestService.getMyRequests(loginUser.getId(), PageRequest.of(0, 10)).getContent());


        // 폼 바인딩 기본값
        if (!model.containsAttribute("profileForm")) {
            var pf = new ProfileForm();
            pf.setEmail(loginUser.getEmail());
            pf.setNickname(loginUser.getNickname());
            model.addAttribute("profileForm", pf);
        }
        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new PasswordForm());
        }
        if (!model.containsAttribute("settingsForm")) {
            var sf = userService.loadSettings(loginUser.getId()); // emailNotify/socialNotify/profilePublic 포함 DTO 반환 가정
            model.addAttribute("settingsForm", sf != null ? sf : new SettingsForm());
        }

        // 어떤 탭을 열지 (선택)
        model.addAttribute("activeTab", tab == null ? "overview" : tab);

        return "user/mypage";
    }

    /** 프로필(닉네임/이메일/프로필 이미지) 업데이트 */
    @PostMapping("/me/profile")
    public String updateProfile(
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser,
            @Valid @ModelAttribute("profileForm") ProfileForm form,
            BindingResult binding,
            @RequestParam(name = "profileImage", required = false) MultipartFile profileImage,
            RedirectAttributes ra
    ) throws IOException {
        if (loginUser == null) return "redirect:/login?redirect=/me";
        if (binding.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.profileForm", binding);
            ra.addFlashAttribute("profileForm", form);
            ra.addFlashAttribute("error", "입력을 확인해 주세요.");
            return "redirect:/me?tab=account";
        }

        String imageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            imageUrl = userService.uploadProfileImage(loginUser.getId(), profileImage);
        }

        loginUser.setProfileImage(imageUrl);
        userService.updateProfile(loginUser.getId(), form.getEmail(), form.getNickname(), imageUrl);
        ra.addFlashAttribute("toast", "프로필이 저장되었습니다.");
        return "redirect:/me?tab=account";
    }

    /** 비밀번호 변경 */
    @PostMapping("/me/password")
    public String changePassword(
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser,
            @Valid @ModelAttribute("passwordForm") UserViewController.PasswordForm form,
            BindingResult binding, // ⚠️ 반드시 @ModelAttribute 바로 다음에!
            RedirectAttributes ra
    ) {
        if (loginUser == null) return "redirect:/login?redirect=/me";

        // 1) 기본 검증
        if (form.getNewPassword() == null || form.getNewPassword().length() < 8) {
            binding.rejectValue("newPassword", "size", "새 비밀번호는 8자 이상이어야 합니다.");
        }
        if (form.getConfirmPassword() == null || !form.getConfirmPassword().equals(form.getNewPassword())) {
            binding.rejectValue("confirmPassword", "mismatch", "새 비밀번호가 일치하지 않습니다.");
        }

        // 에러가 있으면 플래시로 되돌리기
        if (binding.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.passwordForm", binding);
            ra.addFlashAttribute("passwordForm", form);
            ra.addFlashAttribute("error", "비밀번호 변경에 실패했습니다. 입력을 확인하세요.");
            return "redirect:/me?tab=account";
        }

        // 2) 서비스 호출
        try {
            userService.changePassword(loginUser.getId(), form.getCurrentPassword(), form.getNewPassword());
            ra.addFlashAttribute("toast", "비밀번호가 변경되었습니다.");
            return "redirect:/me?tab=account";
        } catch (BadCredentialException e) {
            // 현재 비밀번호 틀림 → 필드 에러로 다시 보내기
            binding.rejectValue("currentPassword", "badCurrent", "현재 비밀번호가 올바르지 않습니다.");
            ra.addFlashAttribute("org.springframework.validation.BindingResult.passwordForm", binding);
            ra.addFlashAttribute("passwordForm", form);
            return "redirect:/me?tab=account";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "비밀번호 변경 중 오류가 발생했습니다.");
            return "redirect:/me?tab=account";
        }
    }


    /** 알림/개인정보 설정 저장 */
    @PostMapping("/me/settings")
    public String saveSettings(
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser,
            @Valid @ModelAttribute("settingsForm") SettingsForm form,
            BindingResult binding,
            RedirectAttributes ra
    ) {
        if (loginUser == null) return "redirect:/login?redirect=/me";
        if (binding.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.settingsForm", binding);
            ra.addFlashAttribute("settingsForm", form);
            ra.addFlashAttribute("error", "설정 저장에 실패했습니다.");
            return "redirect:/me?tab=settings";
        }
        userService.saveSettings(loginUser.getId(), form);
        ra.addFlashAttribute("toast", "설정이 저장되었습니다.");
        return "redirect:/me?tab=settings";
    }

/*    *//** 특정 로그인 세션(기기) 강제 로그아웃 *//*
    @PostMapping("/me/sessions/revoke")
    public String revokeSession(
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser,
            @RequestParam("sessionId") String sessionId,
            RedirectAttributes ra
    ) {
        if (loginUser == null) return "redirect:/login?redirect=/me";
        sessionService.revokeSession(loginUser.getId(), sessionId);
        ra.addFlashAttribute("toast", "선택한 기기에서 로그아웃되었습니다.");
        return "redirect:/me?tab=sessions";
    }*/

    /** 계정 비활성화 */
    @PostMapping("/me/deactivate")
    public String deactivate(
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser,
            RedirectAttributes ra
    ) {
        if (loginUser == null) return "redirect:/login?redirect=/me";
        userService.deactivate(loginUser.getId());
        ra.addFlashAttribute("toast", "계정이 비활성화되었습니다.");
        return "redirect:/me";
    }

    /** 계정 완전 삭제 */
    @PostMapping("/me/delete")
    public String deleteAccount(
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser
    ) {
        if (loginUser == null) return "redirect:/login?redirect=/me";
        userService.deleteAccount(loginUser.getId());
        // 세션 무효화 & 로그아웃 경로로
        return "redirect:/login";
    }

    /* ---------- DTOs ---------- */

    @Data
    public static class StatsDto {
        private final long postCount;
        private final long commentCount;
        private final long boardRequestCount;
    }

    @Data
    public static class ProfileForm {
        @Email(message = "이메일 형식을 확인해 주세요.")
        @NotBlank(message = "이메일은 필수입니다.")
        private String email;

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자입니다.")
        private String nickname;
    }

    @Data
    public static class PasswordForm {
        @NotBlank(message = "현재 비밀번호를 입력하세요.")
        private String currentPassword;

        @Size(min = 8, max = 64, message = "새 비밀번호는 8자 이상이어야 합니다.")
        private String newPassword;

        private String confirmPassword;
    }

    /** 알림/개인정보 설정 */
    @Data
    public static class SettingsForm {
        private boolean emailNotify;
        private boolean socialNotify;
        private boolean profilePublic;
    }
}

