package myproject.booktalk.bookSearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myproject.booktalk.bookSearch.dto.BookDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookSearchController {

    private final BookSearchService bookSearchService;

    // 검색창만
    @GetMapping("/search")
    public String searchPage() {
        return "book/search";
    }

    // 검색 실행
    @GetMapping(value = "/search", params = "query")
    public String doSearch(@RequestParam String query, Model model) {
        // 디버그: 받은 문자열 그대로/유니코드 포인트 확인
        log.info("[BookSearch] received query='{}'", query);
        log.info("[BookSearch] codepoints={}", query.chars()
                .mapToObj(cp -> String.format("U+%04X", cp))
                .toList());

        var result = bookSearchService.searchBooks(query.trim());
        model.addAttribute("result", result);
        model.addAttribute("q", query);
        return "book/search";
    }
}

