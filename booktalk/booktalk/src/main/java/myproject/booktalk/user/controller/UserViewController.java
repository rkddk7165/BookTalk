package myproject.booktalk.user.controller;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.user.User;
import myproject.booktalk.user.session.SessionConst;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequiredArgsConstructor
public class UserViewController {

    @GetMapping("/me")
    public String myPage(@SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser,
                         Model model) {
        if (loginUser == null) return "redirect:/login?redirect=/me";
        model.addAttribute("user", loginUser);
        return "user/mypage";
    }
}
