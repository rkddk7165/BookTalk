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
    public String join(@Valid @ModelAttribute("joinRequest") JoinRequest form,
                       BindingResult bindingResult,
                       RedirectAttributes ra) {
        if (bindingResult.hasErrors()) return "auth/join";

        User joinUser = form.toEntity();

        userService.join(joinUser);
        ra.addAttribute("email", form.getEmail());
        return "redirect:/login";
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
