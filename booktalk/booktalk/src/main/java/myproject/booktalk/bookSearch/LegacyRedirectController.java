package myproject.booktalk.bookSearch;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegacyRedirectController {
    @GetMapping("/book/search")
    public String legacy() {
        return "redirect:/books/search"; // 복수로 정정
    }
}
