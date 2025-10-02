    package myproject.booktalk.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ActiveBoardItem {
    private Long boardId;
    private String title;   // 보드 제목(없으면 책 제목을 컨트롤러에서 세팅해도 ok)
    private String author;  // optional
    private long postCount; // 게시글 수
}
