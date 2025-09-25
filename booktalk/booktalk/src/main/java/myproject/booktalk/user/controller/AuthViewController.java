package myproject.booktalk.user.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myproject.booktalk.kakao.KakaoLoginService;
import myproject.booktalk.user.User;
import myproject.booktalk.user.UserService;
import myproject.booktalk.user.dto.JoinRequest;
import myproject.booktalk.user.dto.LoginRequest;
import myproject.booktalk.user.exception.UserException;
import myproject.booktalk.user.session.SessionConst;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthViewController {

    private final UserService userService;
    private final KakaoLoginService kakaoLoginService;

    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "redirect", required = false) String redirect,
                            @RequestParam(value = "error", required = false) String error,
                            Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        model.addAttribute("redirect", redirect);
        model.addAttribute("error", error);
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginRequest") LoginRequest form,
                        BindingResult bindingResult,
                        @RequestParam(value = "redirect", required = false) String redirect,
                        HttpServletRequest request,
                        RedirectAttributes ra) {

        log.info(form.getEmail());
        log.info(form.getPassword());

        if (bindingResult.hasErrors()) {
            log.info(bindingResult.getAllErrors().toString());
            return "auth/login";
        }



        try {
            User user = userService.login(form.getEmail(), form.getPassword());
            log.info(user.toString());
            HttpSession session = request.getSession();
            session.setAttribute(SessionConst.LOGIN_USER, user);
            return "redirect:" + (StringUtils.hasText(redirect) ? redirect : "/");
        } catch (UserException e) {
            ra.addAttribute("error", e.getMessage());
            if (StringUtils.hasText(redirect)) ra.addAttribute("redirect", redirect);
            return "redirect:/login";
        }
    }

    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("joinRequest", new JoinRequest());
        return "auth/join";
    }

    @PostMapping("/join")
    public String join(@Valid @ModelAttribute("joinRequest") JoinRequest req,
                       BindingResult binding,                  // ⚠️ @ModelAttribute 바로 다음
                       @RequestParam(name = "profileImage", required = false) MultipartFile profileImage,
                       RedirectAttributes ra) {
        // 0) 사전 정리: 공백/소문자
        if (req.getEmail() != null) req.setEmail(req.getEmail().trim().toLowerCase());
        if (req.getNickname() != null) req.setNickname(req.getNickname().trim());

        // 1) 추가 검증 (필수 동의 & 비밀번호 확인 & 최소 길이)
        if (req.getPassword() == null || req.getPassword().trim().length() < 8) {
            binding.rejectValue("password", "size", "비밀번호는 8자 이상이어야 합니다.");
        }
        if (req.getConfirmPassword() == null || !req.getPassword().equals(req.getConfirmPassword())) {
            binding.rejectValue("confirmPassword", "mismatch", "비밀번호가 일치하지 않습니다.");
        }
        if (req.getOver14() == null || !req.getOver14()) {
            binding.rejectValue("over14", "required", "만 14세 이상에 동의해 주세요.");
        }
        if (req.getAgreeTerms() == null || !req.getAgreeTerms()) {
            binding.rejectValue("agreeTerms", "required", "이용약관에 동의해 주세요.");
        }
        if (req.getAgreePrivacy() == null || !req.getAgreePrivacy()) {
            binding.rejectValue("agreePrivacy", "required", "개인정보 처리방침에 동의해 주세요.");
        }

        if (binding.hasErrors()) {
            log.info(binding.getAllErrors().toString());
            return "auth/join";
        }

        try {
            // 2) 회원 가입 (서비스에서 비밀번호 BCrypt 인코딩)
            Long userId = userService.join(req);

            // 3) 프로필 이미지가 있으면 업로드 후 URL 저장
            if (profileImage != null && !profileImage.isEmpty()) {
                String url = userService.uploadProfileImage(userId, profileImage); // (앞서 만든 메서드 재사용)
                // 이메일/닉네임은 null로 두고 이미지 URL만 반영
                userService.updateProfile(userId, null, null, url);
            }

            ra.addFlashAttribute("toast", "가입이 완료되었습니다. 로그인해 주세요.");
            return "redirect:/login";

        } catch (UserException e) {
            // 예: DUP_EMAIL 등
            binding.rejectValue("email", "duplicate", e.getMessage());
            return "auth/join";
        } catch (org.springframework.web.multipart.MaxUploadSizeExceededException e) {
            binding.rejectValue("profileImage", "tooLarge", "이미지 용량이 큽니다.");
            return "auth/join";
        } catch (Exception e) {
            // 기타 예외는 폼 상단에 표시하고 폼 유지
            binding.reject("joinFailed", "회원가입 처리 중 오류가 발생했습니다.");
            return "auth/join";
        }
    }


    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return "redirect:/";
    }

    // Kakao OAuth (SSR)
    @GetMapping("/oauth2/kakao")
    public String kakaoRedirect() {
        String authorizeUrl = kakaoLoginService.buildAuthorizeUrl();
        return "redirect:" + authorizeUrl;
    }

    @GetMapping("/login/oauth2/code/kakao")
    public String kakaoCallback(@RequestParam("code") String code,
                                HttpServletRequest request) {
        User kakaoUser = kakaoLoginService.kakaoLogin(code);
        request.getSession().setAttribute(SessionConst.LOGIN_USER, kakaoUser);
        return "redirect:/";
    }
}
