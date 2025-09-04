package myproject;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.user.User;
import myproject.booktalk.user.session.SessionConst;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequiredArgsConstructor
public class HomeController {

    public String home(@SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser,
                       Model model) {

        model.addAttribute("loginUser", loginUser);
        return "home";
    }
}
