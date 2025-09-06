package myproject.booktalk.board;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BoardController {

    @GetMapping("/boards/{slug}")
    public String board(@PathVariable String slug, Model model) {
        model.addAttribute("slug", slug); // free | recommend | quotes
        return "board/list";
    }
}