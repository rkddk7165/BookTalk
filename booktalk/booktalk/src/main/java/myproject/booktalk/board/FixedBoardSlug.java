package myproject.booktalk.board;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FixedBoardSlug {
    FREE("free", "자유게시판", "자유롭게 이야기하는 공간입니다."),
    RECOMMEND("recommend", "책추천게시판", "좋은 책을 서로 추천해 주세요."),
    QUOTES("quotes", "한줄글귀게시판", "마음에 남는 한 줄을 공유해 보세요.");

    private final String slug;
    private final String title;
    private final String description;

    public static FixedBoardSlug from(String slug) {
        for (var v : values()) if (v.slug.equals(slug)) return v;
        throw new IllegalArgumentException("지원하지 않는 slug: " + slug);
    }

    /** title → enum */
    public static FixedBoardSlug fromTitle(String title) {
        for (FixedBoardSlug fb : values()) {
            if (fb.title.equalsIgnoreCase(title)) {
                return fb;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 title: " + title);
    }
}