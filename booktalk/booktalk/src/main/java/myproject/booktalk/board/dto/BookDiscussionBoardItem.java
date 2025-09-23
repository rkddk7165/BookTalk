package myproject.booktalk.board.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookDiscussionBoardItem {
    private Long boardId;
    private Long bookId;
    private String title;   // 보드 제목 없으면 책 제목 사용
    private String author;  // (옵션) 카드에 표시용
}